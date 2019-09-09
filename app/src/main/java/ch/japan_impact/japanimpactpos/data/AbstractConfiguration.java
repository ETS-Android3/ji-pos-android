package ch.japan_impact.japanimpactpos.data;

/**
 * @author Louis Vialar
 */
public abstract class AbstractConfiguration {
    protected int id;
    protected int eventId;
    protected String name;

    public AbstractConfiguration(int id, int eventId, String name) {
        this.id = id;
        this.eventId = eventId;
        this.name = name;
    }

    public AbstractConfiguration() {
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

    /**
     * Returns a copy of the configuration with an updated name
     */
    public abstract AbstractConfiguration updateName(String name);
}
