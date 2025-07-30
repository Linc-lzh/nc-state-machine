package nc.sm.core.context;

import nc.sm.core.entity.FileProcessLog;
import nc.sm.core.entity.FileProcessState;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class FileProcessContext {
    private String fileId;
    private String fileName;
    private FileProcessState currentState;
    private Map<String, Object> metadata = new ConcurrentHashMap<>();
    private List<FileProcessLog> logs = new CopyOnWriteArrayList<>();

    // 单元信息
    private String zoneId;
    private String subDBId;
    private String tableId;

    // 文件处理指标
    private AtomicLong processedRecords = new AtomicLong(0);
    private AtomicLong successRecords = new AtomicLong(0);
    private AtomicLong failedRecords = new AtomicLong(0);

    // getters and setters...


    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileProcessState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(FileProcessState currentState) {
        this.currentState = currentState;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public List<FileProcessLog> getLogs() {
        return logs;
    }

    public void setLogs(List<FileProcessLog> logs) {
        this.logs = logs;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getSubDBId() {
        return subDBId;
    }

    public void setSubDBId(String subDBId) {
        this.subDBId = subDBId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public AtomicLong getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(AtomicLong processedRecords) {
        this.processedRecords = processedRecords;
    }

    public AtomicLong getSuccessRecords() {
        return successRecords;
    }

    public void setSuccessRecords(AtomicLong successRecords) {
        this.successRecords = successRecords;
    }

    public AtomicLong getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(AtomicLong failedRecords) {
        this.failedRecords = failedRecords;
    }

    public void addLog(String event, String message) {
        FileProcessLog log = new FileProcessLog(
                System.currentTimeMillis(),
                this.currentState,
                event,
                message,
                Thread.currentThread().getName()
        );
        logs.add(log);
    }
}



