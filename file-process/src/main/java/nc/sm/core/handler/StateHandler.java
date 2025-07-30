package nc.sm.core.handler;

import nc.sm.core.context.FileProcessContext;
import nc.sm.core.entity.RetryPolicy;

public interface StateHandler {
    /**
     * 处理状态逻辑
     */
    void handle(FileProcessContext context) throws FileProcessException;

    /**
     * 状态处理器名称
     */
    String getName();

    /**
     * 是否支持异步处理
     */
    default boolean isAsync() {
        return false;
    }

    /**
     * 出错时的重试策略
     */
    default RetryPolicy getRetryPolicy() {
        return RetryPolicy.DEFAULT;
    }
}


