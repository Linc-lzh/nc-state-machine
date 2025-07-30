package nc.sm.core.entity;

public enum RetryPolicy {
    IMMEDIATE(3, 0), // 立即重试3次
    BACKOFF(5, 1000), // 退避重试，最多5次，间隔1秒
    CUSTOM(3, 500), // 自定义
    NO_RETRY(0, 0); // 不重试

    final int maxAttempts;
    final long backoffPeriod;

    // constructor and getters...

    RetryPolicy(int maxAttempts, long backoffPeriod) {
        this.maxAttempts = maxAttempts;
        this.backoffPeriod = backoffPeriod;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}