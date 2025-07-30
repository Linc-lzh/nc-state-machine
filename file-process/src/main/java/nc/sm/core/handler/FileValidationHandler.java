package nc.sm.core.handler;

import ch.qos.logback.classic.Logger;
import nc.sm.core.context.FileProcessContext;
import nc.sm.core.entity.RetryPolicy;
import nc.sm.core.exception.FileProcessException;
import org.slf4j.LoggerFactory;

public class FileValidationHandler implements StateHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileValidationHandler.class);

    @Override
    public void handle(FileProcessContext context) throws FileProcessException {
        context.addLog("FILE_VALIDATION_START", "Starting file validation");

        try {
            // 1. 验证文件是否存在
            validateFileExists(context);

            // 2. 验证文件格式
            validateFileFormat(context);

            // 3. 验证文件大小
            validateFileSize(context);

            // 4. 验证文件权限
            validateFilePermissions(context);

            context.addLog("FILE_VALIDATION_SUCCESS", "File validation completed successfully");
        } catch (Exception e) {
            context.addLog("FILE_VALIDATION_FAILED", "File validation failed: " + e.getMessage());
            throw new FileProcessException("File validation failed", e);
        }
    }

    private void validateFileExists(FileProcessContext context) {
        // 实现文件存在性验证
    }

    private void validateFileFormat(FileProcessContext context) {
        // 实现文件格式验证
    }

    private void validateFileSize(FileProcessContext context) {
        // 实现文件大小验证
    }

    private void validateFilePermissions(FileProcessContext context) {
        // 实现文件权限验证
    }

    @Override
    public String getName() {
        return "FILE_VALIDATION_HANDLER";
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        return RetryPolicy.NO_RETRY; // 文件验证通常不需要重试
    }
}

