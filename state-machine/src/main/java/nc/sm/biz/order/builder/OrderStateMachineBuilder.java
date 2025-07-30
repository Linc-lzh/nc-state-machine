package nc.sm.biz.order.builder;

import nc.sm.biz.order.entity.OrderEvent;
import nc.sm.biz.order.entity.OrderState;
import nc.sm.biz.order.pojo.Order;
import nc.sm.biz.order.pojo.Payment;
import nc.sm.core.component.State;
import nc.sm.core.component.StateMachine;
import nc.sm.core.component.StateMachineListener;
import nc.sm.core.component.Transition;
import nc.sm.core.component.impl.GenericStateMachine;

public class OrderStateMachineBuilder {

    public static StateMachine<OrderState, OrderEvent> build() {
        // 创建状态
        State<OrderState, OrderEvent> created = new State<>(OrderState.CREATED);
        State<OrderState, OrderEvent> pendingPayment = new State<>(OrderState.PENDING_PAYMENT);
        State<OrderState, OrderEvent> paid = new State<>(OrderState.PAID);
        State<OrderState, OrderEvent> shipped = new State<>(OrderState.SHIPPED);
        State<OrderState, OrderEvent> delivered = new State<>(OrderState.DELIVERED);
        State<OrderState, OrderEvent> cancelled = new State<>(OrderState.CANCELLED);
        State<OrderState, OrderEvent> refunded = new State<>(OrderState.REFUNDED);

        // 创建状态机
        StateMachine<OrderState, OrderEvent> machine =
                new GenericStateMachine<>(OrderState.CREATED);

        // 添加状态转换
        machine.addTransition(new Transition<>(created, pendingPayment, OrderEvent.PAY));

        machine.addTransition(new Transition<>(pendingPayment, paid, OrderEvent.PAY)
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
