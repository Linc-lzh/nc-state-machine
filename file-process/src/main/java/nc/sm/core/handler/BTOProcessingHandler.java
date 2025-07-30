package nc.sm.core.handler;

import ch.qos.logback.classic.Logger;
import nc.sm.core.context.FileProcessContext;
import nc.sm.core.entity.RetryPolicy;
import nc.sm.core.exception.FileProcessException;
import org.slf4j.LoggerFactory;

public class BTOProcessingHandler implements StateHandler {
    private static final Logger logger = LoggerFactory.getLogger(BTOProcessingHandler.class);

    private final OnlineTxnInvoker txnInvoker;
    private final RoutingService routingService;

    public BTOProcessingHandler(OnlineTxnInvoker txnInvoker, RoutingService routingService) {
        this.txnInvoker = txnInvoker;
        this.routingService = routingService;
    }

    @Override
    public void handle(FileProcessContext context) throws FileProcessException {
        context.addLog("BTO_PROCESSING_START", "Starting batch-to-online processing");

        try {
            // 1. 获取路由信息
            RoutingResult routing = routingService.getRoutingInfo(context.getFileId());

            // 2. 根据单元化架构执行分发
            if (routing.isMultiUnit()) {
                processMultiUnit(context, routing);
            } else {
                processSingleUnit(context, routing);
            }

            context.addLog("BTO_PROCESSING_SUCCESS", "Batch-to-online processing completed");
        } catch (Exception e) {
            context.addLog("BTO_PROCESSING_FAILED", "Batch-to-online processing failed: " + e.getMessage());
            throw new FileProcessException("BTO processing failed", e);
        }
    }

    private void processSingleUnit(FileProcessContext context, RoutingResult routing) {
        // 实现单一单元处理逻辑
    }

    private void processMultiUnit(FileProcessContext context, RoutingResult routing) {
        // 实现多单元分发处理逻辑
    }

    @Override
    public String getName() {
        return "BTO_PROCESSING_HANDLER";
    }

    @Override
    public boolean isAsync() {
        return true; // BTO处理通常可以异步执行
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        return RetryPolicy.BACKOFF; // BTO处理适合退避重试
    }
}

