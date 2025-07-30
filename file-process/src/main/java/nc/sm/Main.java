package nc.sm;

import nc.sm.core.config.StateMachineConfig;
import nc.sm.core.entity.FileProcessState;

public class Main {
    public static void main(String[] args) {
        // 1. 创建配置
        StateMachineConfig config = new StateMachineConfig();

        // 2. 添加自定义处理器（可选）
        config.addCustomHandler(FileProcessState.FILE_PARSING, new CustomFileParser());

        // 3. 添加自定义转移（可选）
        config.addCustomTransition(FileProcessState.BTO_PROCESSING, FileProcessState.FILE_RECONCILIATION);

        // 4. 创建服务

    }