package ch.japan_impact.japanimpactpos.data;

import java.util.List;

/**
 * @author Louis Vialar
 */
public abstract class AbstractConfigurationList<T extends AbstractConfiguration> {
    private Event event;
    private List<T> configs;

    public AbstractConfigurationList(Event event, List<T> configs) {
        this.event = event;
        this.configs = configs;
    }

    public AbstractConfigurationList() {
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<T> getConfigs() {
        return configs;
    }

    public void setConfigs(List<T> configs) {
        this.configs = configs;
    }
}
