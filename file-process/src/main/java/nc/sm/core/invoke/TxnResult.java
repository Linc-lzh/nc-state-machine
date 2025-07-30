package nc.sm.core.invoke;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 交易结果对象
 */
public class TxnResult implements Serializable {
    private boolean success;
    private String code;
    private String message;
    private String txnNo;
    private LocalDateTime finishTime;

    // 静态工厂方法
    public static TxnResult success(String txnNo) {
        return new TxnResult(true, "SUCCESS", "处理成功", txnNo, LocalDateTime.now());
    }

    public static TxnResult failure(String code, String message) {
        return new TxnResult(false, code, message, null, LocalDateTime.now());
    }

    // constructor and getters...

    public TxnResult(boolean success, String code, String message, String txnNo, LocalDateTime finishTime) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.txnNo = txnNo;
        this.finishTime = finishTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTxnNo() {
        return txnNo;
    }

    public void setTxnNo(String txnNo) {
        this.txnNo = txnNo;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }
}
