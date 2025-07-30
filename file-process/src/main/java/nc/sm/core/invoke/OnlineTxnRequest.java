package nc.sm.core.invoke;

import java.io.Serializable;

/**
 * 交易请求对象
 */
public class OnlineTxnRequest implements Serializable {
    private String txnType;       // 交易类型：BATCH_DEBIT等
    private String txnData;       // JSON格式交易数据
    private String bizNo;         // 业务流水号
    private String fileRefNo;     // 关联文件编号

    // getters and setters...

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getTxnData() {
        return txnData;
    }

    public void setTxnData(String txnData) {
        this.txnData = txnData;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public String getFileRefNo() {
        return fileRefNo;
    }

    public void setFileRefNo(String fileRefNo) {
        this.fileRefNo = fileRefNo;
    }
}
