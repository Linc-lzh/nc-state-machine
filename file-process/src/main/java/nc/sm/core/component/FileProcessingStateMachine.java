package nc.sm.core.component;

import nc.sm.core.config.StateMachineConfig;
import nc.sm.core.context.FileProcessContext;
import nc.sm.core.entity.FileProcessState;

// 状态机核心接口
public interface FileProcessingStateMachine {

    /**
     * 执行状态转移
     * @param context 处理上下文
     * @return 下一个状态
     */
    FileProcessState transition(FileProcessContext context);

    /**
     * 获取当前状态
     */
    FileProcessState getCurrentState();

    /**
     * 状态机配置
     */
    StateMachineConfig getConfig();
}
