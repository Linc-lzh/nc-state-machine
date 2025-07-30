package nc.sm.core.config;

import nc.sm.core.entity.FileProcessState;
import nc.sm.core.handler.StateHandler;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StateMachineConfig {
    private ExecutorService asyncExecutor;
    private Map<FileProcessState, StateHandler> customHandlers = new EnumMap<>(FileProcessState.class);
    private Map<FileProcessState, FileProcessState> customTransitions = new EnumMap<>(FileProcessState.class);

    public void addCustomHandler(FileProcessState state, StateHandler handler) {
        customHandlers.put(state, handler);
    }

    public void addCustomTransition(FileProcessState from, FileProcessState to) {
        customTransitions.put(from, to);
    }

    // 默认异步执行器配置
    public ExecutorService getDefaultAsyncExecutor() {
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        int maxPoolSize = corePoolSize * 4;
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    // getters and setters...

    public ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    public void setAsyncExecutor(ExecutorService asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }

    public Map<FileProcessState, StateHandler> getCustomHandlers() {
        return customHandlers;
    }

    public void setCustomHandlers(Map<FileProcessState, StateHandler> customHandlers) {
        this.customHandlers = customHandlers;
    }

    public Map<FileProcessState, FileProcessState> getCustomTransitions() {
        return customTransitions;
    }

    public void setCustomTransitions(Map<FileProcessState, FileProcessState> customTransitions) {
        this.customTransitions = customTransitions;
    }
}
