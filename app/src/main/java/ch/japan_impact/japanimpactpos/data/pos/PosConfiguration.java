package ch.japan_impact.japanimpactpos.data.pos;

import ch.japan_impact.japanimpactpos.data.AbstractConfiguration;

/**
 * @author Louis Vialar
 */
public class PosConfiguration extends AbstractConfiguration {
    private boolean acceptCards;

    public PosConfiguration() {
        super();
    }

    public PosConfiguration(int id, int eventId, String name, boolean acceptCards) {
        super(id, eventId, name);
        this.acceptCards = acceptCards;
    }

    public boolean isAcceptCards() {
        return acceptCards;
    }

    public void setAcceptCards(boolean acceptCards) {
        this.acceptCards = acceptCards;
    }

    @Override
    public AbstractConfiguration updateName(String name) {
        return new PosConfiguration(this.id, this.eventId, name, this.acceptCards);
    }
}
