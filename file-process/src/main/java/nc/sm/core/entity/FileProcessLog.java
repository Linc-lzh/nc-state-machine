package nc.sm.core.entity;

public class FileProcessLog {
    private final long timestamp;
    private final FileProcessState state;
    private final String event;
    private final String message;
    private final String threadName;

    // constructor and getters...

    public FileProcessLog(long timestamp, FileProcessState state, String event, String message, String threadName) {
        this.timestamp = timestamp;
        this.state = state;
        this.event = event;
        this.message = message;
        this.threadName = threadName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public FileProcessState getState() {
        return state;
    }

    public String getEvent() {
        return event;
    }

    public String getMessage() {
        return message;
    }

    public String getThreadName() {
        return threadName;
    }
}