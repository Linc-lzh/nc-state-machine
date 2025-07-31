package nc.sm.biz.file.entity;

public enum FileProcessState {
    INIT("init", "初始化状态"),
    FILE_VALIDATION("file_validation", "文件验证"),
    FILE_REGISTRATION("file_registration", "文件注册"),
    FILE_PARSING("file_parsing", "文件解析"),
    BTO_PROCESSING("bto_processing", "批量转在线处理"),
    FILE_RECONCILIATION("file_reconciliation", "文件回盘"),
    FILE_MERGING("file_merging", "文件合并"),
    FILE_UPLOAD("file_upload", "文件上传"),
    COMPLETED("completed", "处理完成"),
    FAILED("failed", "处理失败"),
    RETRY("retry", "重试状态");

    private final String code;
    private final String desc;

    FileProcessState(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // getters...

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
