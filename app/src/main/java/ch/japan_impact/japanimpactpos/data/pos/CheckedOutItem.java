package ch.japan_impact.japanimpactpos.data.pos;

/**
 * @author Louis Vialar
 */
public class CheckedOutItem {
    private int itemId;
    private int itemAmount;
    private int itemPrice;

    public CheckedOutItem(int itemId, int itemAmount, int itemPrice) {
        this.itemId = itemId;
        this.itemAmount = itemAmount;
        this.itemPrice = itemPrice;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(int itemAmount) {
        this.itemAmount = itemAmount;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }
}
