package ch.japan_impact.japanimpactpos.data;

/**
 * @author Louis Vialar
 */
public class PosConfigResponse {
    private PosConfiguration config;
    private PosItem[] items;

    public PosConfigResponse() {
    }

    public PosConfigResponse(PosConfiguration config, PosItem[] items) {
        this.config = config;
        this.items = items;
    }

    public PosConfiguration getConfig() {
        return config;
    }

    public void setConfig(PosConfiguration config) {
        this.config = config;
    }

    public PosItem[] getItems() {
        return items;
    }

    public void setItems(PosItem[] items) {
        this.items = items;
    }
}
