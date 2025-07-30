package nc.sm.core.handler;

import ch.qos.logback.classic.Logger;
import nc.sm.core.component.DefaultFileProcessingStateMachine;
import nc.sm.core.component.FileProcessingStateMachine;
import nc.sm.core.config.StateMachineConfig;
import nc.sm.core.context.FileProcessContext;
import nc.sm.core.entity.FileProcessLog;
import nc.sm.core.entity.FileProcessState;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FileBatchProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(FileBatchProcessingService.class);

    private final FileProcessingStateMachine stateMachine;
    private final FileProcessRepository processRepository;
    private final FileStorageService storageService;

    public FileBatchProcessingService(StateMachineConfig config,
                                      FileProcessRepository processRepository,
                                      FileStorageService storageService) {
        this.stateMachine = new DefaultFileProcessingStateMachine(config);
        this.processRepository = processRepository;
        this.storageService = storageService;
    }

    @Transactional
    public String startFileProcessing(String fileName, String filePath) {
        String fileId = generateFileId();

        FileProcessContext context = new FileProcessContext();
        context.setFileId(fileId);
        context.setFileName(fileName);
        context.setCurrentState(FileProcessState.INIT);

        // 保存初始状态
        processRepository.save(context);

        // 启动状态机
        executeStateMachine(context);

        return fileId;
    }

    public FileProcessState getProcessState(String fileId) {
        return processRepository.getState(fileId);
    }

    public List<FileProcessLog> getProcessLogs(String fileId) {
        return processRepository.getLogs(fileId);
    }

    private void executeStateMachine(FileProcessContext context) {
        while (true) {
            FileProcessState currentState = stateMachine.getCurrentState();

            if (currentState == FileProcessState.COMPLETED ||
                    currentState == FileProcessState.FAILED) {
                // 最终状态，处理完成
                processRepository.save(context);
                break;
            }

            try {
                FileProcessState newState = stateMachine.transition(context);
                processRepository.save(context);

                if (newState == FileProcessState.COMPLETED) {
                    // 处理完成
                    storageService.cleanupTempFiles(context.getFileId());
                }
            } catch (Exception e) {
                logger.error("File processing failed", e);
                context.addLog("PROCESS_FAILED", "Processing failed: " + e.getMessage());
                context.setCurrentState(FileProcessState.FAILED);
                processRepository.save(context);
                break;
            }
        }
    }

    private String generateFileId() {
        return UUID.randomUUID().toString();
    }
}

