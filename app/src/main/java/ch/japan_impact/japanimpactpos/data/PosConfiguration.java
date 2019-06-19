package ch.japan_impact.japanimpactpos.data;

/**
 * @author Louis Vialar
 */
public class PosConfiguration {
    private int id;
    private int eventId;
    private String name;
    private boolean acceptCards;

    public PosConfiguration() {
    }

    public PosConfiguration(int id, int eventId, String name, boolean acceptCards) {
        this.id = id;
        this.eventId = eventId;
        this.name = name;
        this.acceptCards = acceptCards;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAcceptCards() {
        return acceptCards;
    }

    public void setAcceptCards(boolean acceptCards) {
        this.acceptCards = acceptCards;
    }
}
