package nc.sm.biz.file.service.impl;

import nc.sm.biz.file.builder.FileStateMachineBuilder;
import nc.sm.biz.file.entity.FileProcessEvent;
import nc.sm.biz.file.entity.FileProcessState;
import nc.sm.biz.file.pojo.FileInfo;
import nc.sm.biz.file.pojo.FileProcessContext;
import nc.sm.core.component.StateMachine;

public class FileService {

    private final StateMachine<FileProcessState, FileProcessEvent> stateMachine;

    public FileService() {
        this.stateMachine = FileStateMachineBuilder.build();
    }

    public void processEvent(FileProcessEvent event, FileProcessContext context) {
        try {
            stateMachine.fire(event, context);
        } catch (Exception e) {
            System.err.println("状态机处理错误: " + e.getMessage());
            // 处理异常情况
        }
    }

    public FileProcessState getCurrentState() {
        return stateMachine.getCurrentState();
    }

    public static void main(String[] args) {
        FileService service = new FileService();
        FileProcessContext context = new FileProcessContext();
        System.out.println("初始状态: " + service.getCurrentState());

        FileInfo fileInfo = new FileInfo("/home", "batonl.txt");
        context.setFileInfo(fileInfo);
        service.processEvent(FileProcessEvent.READ_INIT_FILE, context);
        System.out.println("当前状态: " + service.getCurrentState());

        service.processEvent(FileProcessEvent.INIT_FILE_REGISTER, context);
        System.out.println("当前状态: " + service.getCurrentState());

        service.processEvent(FileProcessEvent.BATCH_PROCESSING, context);
        System.out.println("当前状态: " + service.getCurrentState());

        service.processEvent(FileProcessEvent.SUMMARY_FILE_GENERATE, context);
        System.out.println("最终状态: " + service.getCurrentState());
    }
}
