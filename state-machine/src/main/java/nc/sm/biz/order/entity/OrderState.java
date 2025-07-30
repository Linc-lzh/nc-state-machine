package nc.sm.biz.order.entity;

public enum OrderState {
    CREATED,
    PENDING_PAYMENT,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
