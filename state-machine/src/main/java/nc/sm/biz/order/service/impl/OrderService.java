package nc.sm.biz.order.service.impl;

import nc.sm.biz.order.builder.OrderStateMachineBuilder;
import nc.sm.biz.order.entity.OrderEvent;
import nc.sm.biz.order.entity.OrderState;
import nc.sm.biz.order.pojo.Payment;
import nc.sm.core.component.StateMachine;

public class OrderService {
    private final StateMachine<OrderState, OrderEvent> stateMachine;

    public OrderService() {
        this.stateMachine = OrderStateMachineBuilder.build();
    }

    public void processEvent(OrderEvent event, Object context) {
        try {
            stateMachine.fire(event, context);
        } catch (Exception e) {
            System.err.println("状态机处理错误: " + e.getMessage());
            // 处理异常情况
        }
    }

    public OrderState getCurrentState() {
        return stateMachine.getCurrentState();
    }

    public static void main(String[] args) {
        OrderService service = new OrderService();
        System.out.println("初始状态: " + service.getCurrentState());

        // 支付事件（携带支付上下文）
        Payment payment = new Payment(100.0, "VISA-1234");
        service.processEvent(OrderEvent.PAY, payment);
        System.out.println("当前状态: " + service.getCurrentState());

        // 发货事件
        service.processEvent(OrderEvent.SHIP, null);
        System.out.println("当前状态: " + service.getCurrentState());

        // 尝试在已发货状态下取消订单（应失败）
        service.processEvent(OrderEvent.CANCEL, null);

        // 配送完成
        service.processEvent(OrderEvent.DELIVER, null);
        System.out.println("最终状态: " + service.getCurrentState());
    }
}
