package nc.sm.core.invoke;

import nc.sm.core.context.FileProcessContext;

import java.util.concurrent.CompletableFuture;

/**
 * 在线交易调用器接口
 */
public interface OnlineTxnInvoker {
    /**
     * 同步调用在线交易
     */
    TxnResult invoke(OnlineTxnRequest request, FileProcessContext context);
    /**
     * 异步调用在线交易
     */
    CompletableFuture<TxnResult> invokeAsync(OnlineTxnRequest request, FileProcessContext context);
}
