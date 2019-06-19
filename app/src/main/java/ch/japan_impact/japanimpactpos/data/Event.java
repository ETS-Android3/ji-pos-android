package ch.japan_impact.japanimpactpos.data;

/**
 * @author Louis Vialar
 */
public class Event {
    private Integer id;
    private String name;
    private String location;
    private boolean visible;
    private boolean archived;

    public Event() {
    }

    public Event(Integer id, String name, String location, boolean visible, boolean archived) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.visible = visible;
        this.archived = archived;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
