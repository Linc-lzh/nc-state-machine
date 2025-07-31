package nc.sm.biz.file.builder;

import nc.sm.biz.file.config.StateMachineConfig;
import nc.sm.biz.file.entity.FileProcessEvent;
import nc.sm.biz.file.entity.FileProcessState;
import nc.sm.biz.file.exception.FileProcessException;
import nc.sm.biz.file.pojo.FileInfo;
import nc.sm.biz.file.pojo.FileProcessContext;
import nc.sm.biz.file.pojo.RoutingResult;
import nc.sm.biz.file.repository.FileLogRepository;
import nc.sm.biz.file.repository.FileRepository;
import nc.sm.biz.file.service.impl.RoutingService;
import nc.sm.core.component.State;
import nc.sm.core.component.StateMachine;
import nc.sm.core.component.StateMachineListener;
import nc.sm.core.component.Transition;
import nc.sm.core.component.impl.GenericStateMachine;

public class FileStateMachineBuilder {

    public static StateMachine<FileProcessState, FileProcessEvent> build() {
        FileLogRepository fileLogRepository = new FileLogRepository();
        FileRepository fileRepository = new FileRepository();
        StateMachineConfig config = new StateMachineConfig();
        // 创建状态
        State<FileProcessState, FileProcessEvent> init = new State<>(FileProcessState.INIT);
        State<FileProcessState, FileProcessEvent> validate = new State<>(FileProcessState.FILE_VALIDATION);
        State<FileProcessState, FileProcessEvent> register = new State<>(FileProcessState.FILE_REGISTRATION);
        State<FileProcessState, FileProcessEvent> parsing = new State<>(FileProcessState.FILE_PARSING);
        State<FileProcessState, FileProcessEvent> btoProcessing = new State<>(FileProcessState.BTO_PROCESSING);
        State<FileProcessState, FileProcessEvent> merging = new State<>(FileProcessState.FILE_MERGING);
        State<FileProcessState, FileProcessEvent> upload = new State<>(FileProcessState.FILE_UPLOAD);

        // 创建状态机
        StateMachine<FileProcessState, FileProcessEvent> machine = new GenericStateMachine<>(FileProcessState.INIT, config);

        // 添加状态转换
        machine.addTransition(new Transition<>(init, validate, FileProcessEvent.READ_INIT_FILE));

        machine.addTransition(new Transition<>(validate, register, FileProcessEvent.INIT_FILE_REGISTER)
                .withGuard(context -> {
                    FileInfo fileInfo = ((FileProcessContext) context).getFileInfo();
                    return !fileInfo.getRemoteFileName().equals("");
                })
                .withAction((state, event, context) -> {
                            var con = (FileProcessContext) context;
                            con.addLog("BTO_PROCESSING_START", "Starting batch-to-online processing");
                            RoutingService routingService = new RoutingService();
                            try {
                                // 1. 获取路由信息
                                RoutingResult routing = routingService.getRoutingInfo(con.getFileId());

                                // 2. 根据单元化架构执行分发
                                if (routing.isMultiUnit()) {
                                    processMultiUnit(con, routing);
                                } else {
                                    processSingleUnit(con, routing);
                                }
                                fileLogRepository.save(((FileProcessContext) context).getFileInfo());
                                System.out.println("Read Unit File Successfully");
                                con.addLog("BTO_PROCESSING_SUCCESS", "Batch-to-online processing completed");
                            } catch (Exception e) {
                                con.addLog("BTO_PROCESSING_FAILED", "Batch-to-online processing failed: " + e.getMessage());
                                throw new FileProcessException("BTO processing failed");
                            }

                        }
                ));

        machine.addTransition(new Transition<>(register, parsing, FileProcessEvent.BATCH_PROCESSING)
                .withAction((state, event, context) -> {
                            fileLogRepository.save(((FileProcessContext) context).getFileInfo());
                            System.out.println("Batch Process File Successfully");
                        }
                ));

        machine.addTransition(new Transition<>(parsing, btoProcessing, FileProcessEvent.BATCH_PROCESSING)
                .withAction((state, event, context) -> {
                    fileLogRepository.save(((FileProcessContext) context).getFileInfo());
                    System.out.println("Batch Process File Successfully");
                }));

        machine.addTransition(new Transition<>(btoProcessing, merging, FileProcessEvent.SUMMARY_FILE_GENERATE)
                .withAction((state, event, context) -> {
                    fileLogRepository.save(((FileProcessContext) context).getFileInfo());
                    System.out.println("Write Unit File Successfully");
                }));

        machine.addTransition(new Transition<>(merging, upload, FileProcessEvent.SUMMARY_FILE_UPLOAD)
                .withAction((state, event, context) -> {
                    fileLogRepository.save(((FileProcessContext) context).getFileInfo());
                    System.out.println("Upload Summary File Successfully");
                }));

        // 添加监听器
        machine.addStateListener(new StateMachineListener<>() {
            @Override
            public void onTransitionComplete(FileProcessState from, FileProcessState to, FileProcessEvent event, Object context) {
                System.out.printf("状态转换: %s -> %s (事件: %s)%n", from, to, event);
                fileRepository.updateFileState(((FileProcessContext) context).getFileInfo());
            }

            @Override
            public void onTransitionDenied(FileProcessState from, FileProcessEvent event, Object context) {
                System.err.printf("状态转换被拒绝: %s (事件: %s)%n", from, event);
                fileRepository.updateFileState(((FileProcessContext) context).getFileInfo());
            }
        });

        return machine;
    }

    private static void processSingleUnit(FileProcessContext context, RoutingResult routing) {
        System.out.println("processSingleUnit");
    }

    private static void processMultiUnit(FileProcessContext context, RoutingResult routing) {
        System.out.println("processMultiUnit");
    }
}
