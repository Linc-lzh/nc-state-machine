package nc.sm.biz.order.pojo;

public class Payment {
    private double amount;
    private String cardNumber;

    public Payment(double amount, String cardNumber) {
        this.amount = amount;
        this.cardNumber = cardNumber;
    }

    public double getAmount() { return amount; }

    public boolean isValid() {
        return cardNumber != null && cardNumber.startsWith("VISA");
    }
}
