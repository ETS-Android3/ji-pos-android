package ch.japan_impact.japanimpactpos.data;

/**
 * @author Louis Vialar
 */
public class JIItem {
    private int id;
    private String name;
    private int price;
    // Skipped: description, longDescription, maxItems, eventId, isTicket, freePrice, isVisible

    public JIItem(int id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public JIItem() {
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }
}
