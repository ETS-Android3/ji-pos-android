package ch.japan_impact.japanimpactpos.data.scan;

import ch.japan_impact.japanimpactpos.data.AbstractConfiguration;

/**
 * @author Louis Vialar
 */
public class ScanConfiguration extends AbstractConfiguration {

    public ScanConfiguration() {
        super();
    }

    public ScanConfiguration(int id, int eventId, String name) {
        super(id, eventId, name);
    }

    @Override
    public AbstractConfiguration updateName(String name) {
        return new ScanConfiguration(this.id, this.eventId, name);
    }
}
