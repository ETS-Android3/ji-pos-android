package ch.japan_impact.japanimpactpos.data.pos;

import ch.japan_impact.japanimpactpos.data.Event;

import java.util.List;

/**
 * @author Louis Vialar
 */
public class PosConfigurationList {
    private Event event;
    private List<PosConfiguration> configs;

    public PosConfigurationList(Event event, List<PosConfiguration> configs) {
        this.event = event;
        this.configs = configs;
    }

    public PosConfigurationList() {
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<PosConfiguration> getConfigs() {
        return configs;
    }

    public void setConfigs(List<PosConfiguration> configs) {
        this.configs = configs;
    }
}
