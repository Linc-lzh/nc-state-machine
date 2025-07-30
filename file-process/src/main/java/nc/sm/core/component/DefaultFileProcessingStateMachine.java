package nc.sm.core.component;

import nc.sm.core.config.StateMachineConfig;
import nc.sm.core.context.FileProcessContext;
import nc.sm.core.entity.FileProcessState;
import nc.sm.core.entity.RetryPolicy;
import nc.sm.core.exception.FileProcessException;
import nc.sm.core.handler.BTOProcessingHandler;
import nc.sm.core.handler.FileValidationHandler;
import nc.sm.core.handler.StateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultFileProcessingStateMachine implements FileProcessingStateMachine {
    private static final Logger logger = LoggerFactory.getLogger(DefaultFileProcessingStateMachine.class);

    private final StateMachineConfig config;
    private FileProcessState currentState;

    // 状态处理器映射
    private final Map<FileProcessState, StateHandler> stateHandlers;
    // 状态转移映射
    private final Map<FileProcessState, FileProcessState> transitionMap;

    public DefaultFileProcessingStateMachine(StateMachineConfig config) {
        this.config = config;
        this.currentState = FileProcessState.INIT;
        this.stateHandlers = new EnumMap<>(FileProcessState.class);
        this.transitionMap = new EnumMap<>(FileProcessState.class);

        initializeStateHandlers();
        initializeTransitions();
    }

    private void initializeStateHandlers() {
        // 注册各状态处理器
        stateHandlers.put(FileProcessState.FILE_VALIDATION, new FileValidationHandler());
        stateHandlers.put(FileProcessState.FILE_REGISTRATION, new FileRegistrationHandler());
        stateHandlers.put(FileProcessState.FILE_PARSING, new FileParsingHandler());
        stateHandlers.put(FileProcessState.BTO_PROCESSING, new BTOProcessingHandler());
        stateHandlers.put(FileProcessState.FILE_RECONCILIATION, new FileReconciliationHandler());
        stateHandlers.put(FileProcessState.FILE_MERGING, new FileMergingHandler());
        stateHandlers.put(FileProcessState.FILE_UPLOAD, new FileUploadHandler());

        // 配置中可扩展自定义处理器
        config.getCustomHandlers().forEach((state, handler) ->
                stateHandlers.put(state, handler));
    }

    private void initializeTransitions() {
        // 初始化标准状态转移
        transitionMap.put(FileProcessState.INIT, FileProcessState.FILE_VALIDATION);
        transitionMap.put(FileProcessState.FILE_VALIDATION, FileProcessState.FILE_REGISTRATION);
        transitionMap.put(FileProcessState.FILE_REGISTRATION, FileProcessState.FILE_PARSING);
        transitionMap.put(FileProcessState.FILE_PARSING, FileProcessState.BTO_PROCESSING);
        transitionMap.put(FileProcessState.BTO_PROCESSING, FileProcessState.FILE_RECONCILIATION);
        transitionMap.put(FileProcessState.FILE_RECONCILIATION, FileProcessState.FILE_MERGING);
        transitionMap.put(FileProcessState.FILE_MERGING, FileProcessState.FILE_UPLOAD);
        transitionMap.put(FileProcessState.FILE_UPLOAD, FileProcessState.COMPLETED);

        // 配置中可扩展自定义转移规则
        transitionMap.putAll(config.getCustomTransitions());
    }

    @Override
    public synchronized FileProcessState transition(FileProcessContext context) {
        context.addLog("STATE_TRANSITION",
                String.format("Transition from %s started", currentState));

        try {
            StateHandler handler = stateHandlers.get(currentState);
            if (handler == null) {
                throw new IllegalStateException("No handler for state: " + currentState);
            }

            // 执行状态处理器
            if (handler.isAsync()) {
                executeAsync(handler, context);
            } else {
                executeWithRetry(handler, context);
            }

            // 确定下一个状态
            FileProcessState nextState = determineNextState(context);
            context.setCurrentState(nextState);

            context.addLog("STATE_TRANSITION",
                    String.format("Transition from %s to %s completed", currentState, nextState));

            currentState = nextState;
            return nextState;

        } catch (Exception e) {
            context.addLog("STATE_TRANSITION_ERROR",
                    String.format("Transition failed from %s: %s", currentState, e.getMessage()));

            currentState = FileProcessState.FAILED;
            context.setCurrentState(currentState);
            return currentState;
        }
    }

    @Override
    public FileProcessState getCurrentState() {
        return null;
    }

    @Override
    public StateMachineConfig getConfig() {
        return null;
    }

    private void executeWithRetry(StateHandler handler, FileProcessContext context) {
        RetryPolicy retryPolicy = handler.getRetryPolicy();
        int attempts = 0;

        while (true) {
            try {
                attempts++;
                handler.handle(context);
                return;
            } catch (FileProcessException e) {
                if (attempts >= retryPolicy.getMaxAttempts()) {
                    throw e;
                }

                try {
                    Thread.sleep(retryPolicy.getBackoffPeriod());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new FileProcessException("Interrupted during retry", ie);
                }
            }
        }
    }

    private void executeAsync(StateHandler handler, FileProcessContext context) {
        // 使用线程池执行异步处理
        CompletableFuture.runAsync(() -> {
            try {
                executeWithRetry(handler, context);
            } catch (Exception e) {
                logger.error("Async state handling failed", e);
                context.addLog("ASYNC_ERROR", "Async handling failed: " + e.getMessage());
            }
        }, config.getAsyncExecutor());
    }

    private FileProcessState determineNextState(FileProcessContext context) {
        // 1. 首先检查自定义转移规则
        FileProcessState nextState = transitionMap.get(currentState);

        // 2. 允许处理器通过上下文影响状态转移
        if (context.getMetadata().containsKey("next_state")) {
            nextState = (FileProcessState) context.getMetadata().get("next_state");
        }

        // 3. 确保状态有效
        if (nextState == null) {
            throw new IllegalStateException("No next state defined for: " + currentState);
        }

        return nextState;
    }

    // 其他方法实现...
}

