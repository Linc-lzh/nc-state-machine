package nc.sm.core.invoke.impl;

import ch.qos.logback.classic.Logger;
import nc.sm.core.context.FileProcessContext;
import nc.sm.core.invoke.OnlineTxnInvoker;
import nc.sm.core.invoke.OnlineTxnRequest;
import nc.sm.core.invoke.TxnResult;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * 在线交易调用器实现（支持单元化路由）
 */
public class OnlineTxnInvokerImpl implements OnlineTxnInvoker {
    private static final Logger logger = LoggerFactory.getLogger(OnlineTxnInvokerImpl.class);

    // 依赖组件
    private final RestTemplate restTemplate;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final RetryTemplate retryTemplate;
    private final RoutingService routingService;

    // 配置参数
    private final long defaultTimeout = 5000;
    private final int maxRetries = 3;
    private final String[] backupUnits;

    public OnlineTxnInvokerImpl(RestTemplate restTemplate,
                                CircuitBreakerFactory circuitBreakerFactory,
                                RoutingService routingService,
                                RetryTemplate retryTemplate) {
        this.restTemplate = restTemplate;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.routingService = routingService;
        this.retryTemplate = retryTemplate;

        // 初始化重试策略
        this.retryTemplate.setRetryPolicy(new SimpleRetryPolicy(
                maxRetries,
                Collections.singletonMap(TimeoutException.class, true)
        ));
        this.retryTemplate.setBackOffPolicy(new ExponentialBackOffPolicy());

        // 从配置获取备用单元
        this.backupUnits = new String[]{"cell-backup-1", "cell-backup-2"};
    }
    @Override
    public TxnResult invoke(OnlineTxnRequest request, FileProcessContext context) {
        // 1. 获取路由信息
        RoutingResult routing = getRoutingInfo(request.getTxnType(), context.getFileId());

        // 2. 准备调用上下文
        TxnInvokeContext invokeContext = new TxnInvokeContext(request, routing, context);

        // 3. 带熔断的调用
        return circuitBreakerFactory.create("txnInvoker").run(
                () -> retryTemplate.execute(ctx -> doInvoke(invokeContext)),
                throwable -> fallback(invokeContext, throwable)
        );
    }
    @Override
    public CompletableFuture<TxnResult> invokeAsync(OnlineTxnRequest request, FileProcessContext context) {
        return CompletableFuture.supplyAsync(() -> invoke(request, context))
                .exceptionally(ex -> {
                    context.addLog("ASYNC_INVOKE_FAILED", "异步调用失败: " + ex.getMessage());
                    return TxnResult.failure("ASYNC_ERROR", ex.getMessage());
                });
    }
    private RoutingResult getRoutingInfo(String txnType, String fileId) {
        try {
            RoutingResult routing = routingService.getRoutingInfo(fileId);
            if (!routing.isValid()) {
                throw new RoutingException("Invalid routing result");
            }
            return routing;
        } catch (Exception e) {
            logger.warn("Routing failed, using default units", e);
            return new RoutingResult(backupUnits); // 降级策略
        }
    }
    private TxnResult doInvoke(TxnInvokeContext context) throws TimeoutException {
        long startTime = System.currentTimeMillis();
        String targetUrl = buildTargetUrl(context.getRouting(), context.getRequest());

        try {
            // 设置超时
            RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout((int)defaultTimeout)
                    .setConnectTimeout((int)defaultTimeout)
                    .build();

            HttpEntity<OnlineTxnRequest> entity = new HttpEntity<>(
                    context.getRequest(),
                    buildHeaders(context)
            );

            // 执行调用
            ResponseEntity<TxnResult> response = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.POST,
                    entity,
                    TxnResult.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new TxnException("HTTP状态码异常: " + response.getStatusCodeValue());
            }

            logInvokeSuccess(context, startTime);
            return response.getBody();
        } catch (ResourceAccessException e) {
            logInvokeFailure(context, startTime, e);
            throw new TimeoutException("调用超时");
        } catch (Exception e) {
            logInvokeFailure(context, startTime, e);
            throw new TxnException("交易调用异常", e);
        }
    }
    private String buildTargetUrl(RoutingResult routing, OnlineTxnRequest request) {
        // 单元化URL构建示例: http://{unit}.bank.com/txn/{txnType}
        String unit = routing.getPrimaryUnit();
        return String.format("http://%s.bank.com/txn/%s", unit, request.getTxnType());
    }
    private HttpHeaders buildHeaders(TxnInvokeContext context) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-TXN-ID", UUID.randomUUID().toString());
        headers.set("X-FILE-ID", context.getFileId());
        headers.set("X-UNIT-ID", context.getRouting().getPrimaryUnit());
        return headers;
    }
    private TxnResult fallback(TxnInvokeContext context, Throwable throwable) {
        context.getProcessContext().addLog("TXN_FALLBACK",
                "交易降级触发: " + throwable.getMessage());

        // 记录失败指标
        context.getProcessContext().getFailedRecords().incrementAndGet();

        // 根据业务需求选择：
        // 1. 返回静默失败结果
        // 2. 进入异步补偿队列
        // 3. 记录异常事务待人工处理
        return TxnResult.failure("FALLBACK", "系统繁忙，请稍后再试");
    }
    private void logInvokeSuccess(TxnInvokeContext context, long startTime) {
        long cost = System.currentTimeMillis() - startTime;
        logger.info("[成功]交易调用 {} -> {}, 耗时{}ms",
                context.getRequest().getTxnType(),
                context.getRouting().getPrimaryUnit(),
                cost);

        context.getProcessContext().addLog("TXN_INVOKE_SUCCESS",
                String.format("调用单元%s成功, 耗时%dms",
                        context.getRouting().getPrimaryUnit(),
                        cost));
    }
    private void logInvokeFailure(TxnInvokeContext context, long startTime, Exception e) {
        long cost = System.currentTimeMillis() - startTime;
        logger.error("[失败]交易调用 {} -> {}, 耗时{}ms, 原因: {}",
                context.getRequest().getTxnType(),
                context.getRouting().getPrimaryUnit(),
                cost,
                e.getMessage(),
                e);

        context.getProcessContext().addLog("TXN_INVOKE_FAILED",
                String.format("调用单元%s失败: %s",
                        context.getRouting().getPrimaryUnit(),
                        e.getMessage()));
    }
    // 内部上下文封装类
    private static class TxnInvokeContext {
        private final OnlineTxnRequest request;
        private final RoutingResult routing;
        private final FileProcessContext processContext;
        // constructor and getters...
    }
}
