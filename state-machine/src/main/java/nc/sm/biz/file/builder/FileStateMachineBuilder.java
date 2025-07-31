package nc.sm.biz.file.builder;

import nc.sm.biz.file.entity.FileProcessEvent;
import nc.sm.biz.file.entity.FileProcessState;
import nc.sm.biz.order.entity.OrderEvent;
import nc.sm.biz.order.entity.OrderState;
import nc.sm.biz.order.pojo.Order;
import nc.sm.biz.order.pojo.Payment;
import nc.sm.core.component.State;
import nc.sm.core.component.StateMachine;
import nc.sm.core.component.StateMachineListener;
import nc.sm.core.component.Transition;
import nc.sm.core.component.impl.GenericStateMachine;

public class FileStateMachineBuilder {

    public static StateMachine<FileProcessState, FileProcessEvent> build() {
// 创建状态
        State<FileProcessState, FileProcessEvent> init = new State<>(FileProcessState.INIT);
        State<FileProcessState, FileProcessEvent> validate = new State<>(FileProcessState.FILE_VALIDATION);
        State<FileProcessState, FileProcessEvent> register = new State<>(FileProcessState.FILE_REGISTRATION);
        State<FileProcessState, FileProcessEvent> parsing = new State<>(FileProcessState.FILE_PARSING);
        State<FileProcessState, FileProcessEvent> btoProcessing = new State<>(FileProcessState.BTO_PROCESSING);
        State<FileProcessState, FileProcessEvent> merging = new State<>(FileProcessState.FILE_MERGING);
        State<FileProcessState, FileProcessEvent> upload = new State<>(FileProcessState.FILE_UPLOAD);

        // 创建状态机
        StateMachine<FileProcessState, FileProcessEvent> machine =
                new GenericStateMachine<>(FileProcessState.INIT);

        // 添加状态转换
        machine.addTransition(new Transition<>(init, validate, FileProcessEvent.READ_INIT_FILE));

        machine.addTransition(new Transition<>(validate, register, FileProcessEvent.WRITE_UNIT_FILE)
                .withGuard(context -> {
                    Payment payment = (Payment) context;
                    return payment.getAmount() > 0 && payment.isValid();
                })
                .withAction((state, event, context) ->
                        System.out.println("处理支付逻辑")));

        machine.addTransition(new Transition<>(pendingPayment, cancelled, OrderEvent.CANCEL));
        machine.addTransition(new Transition<>(pendingPayment, shipped, OrderEvent.SHIP));

        machine.addTransition(new Transition<>(paid, shipped, OrderEvent.SHIP)
                .withAction((state, event, context) ->
                        System.out.println("生成运单，更新库存")));

        machine.addTransition(new Transition<>(shipped, delivered, OrderEvent.DELIVER)
                .withAction((state, event, context) ->
                        System.out.println("订单完成，结算商家账户")));

        machine.addTransition(new Transition<>(paid, refunded, OrderEvent.RETURN)
                .withGuard(context -> {
                    Order order = (Order) context;
                    return order.isReturnable();
                })
                .withAction((state, event, context) ->
                        System.out.println("处理退货退款")));

        machine.addTransition(new Transition<>(pendingPayment, refunded, OrderEvent.RETURN));

        // 添加监听器
        machine.addStateListener(new StateMachineListener<OrderState, OrderEvent>() {
            @Override
            public void onTransitionComplete(OrderState from, OrderState to, OrderEvent event, Object context) {
                System.out.printf("状态转换: %s -> %s (事件: %s)%n", from, to, event);
            }

            @Override
            public void onTransitionDenied(OrderState from, OrderEvent event, Object context) {
                System.err.printf("状态转换被拒绝: %s (事件: %s)%n", from, event);
            }
        });

        return machine;
    }
}
