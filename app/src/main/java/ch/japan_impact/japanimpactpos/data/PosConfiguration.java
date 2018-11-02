package ch.japan_impact.japanimpactpos.data;

/**
 * @author Louis Vialar
 */
public class PosConfiguration {
    private int id;
    private String name;

    public PosConfiguration(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public PosConfiguration() {
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
