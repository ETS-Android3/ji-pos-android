package ch.japan_impact.japanimpactpos.data.pos;

/**
 * @author Louis Vialar
 */
public class PosOrderResponse {
    private int orderId;
    private int price;

    public PosOrderResponse(int orderId, int price) {
        this.orderId = orderId;
        this.price = price;
    }

    public PosOrderResponse() {
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
