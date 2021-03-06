package won.bot.framework.eventbot.event.impl.analyzation.agreement;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import won.bot.framework.eventbot.event.BaseNeedAndConnectionSpecificEvent;
import won.protocol.model.Connection;

import java.net.URI;

/**
 * Created by fsuda on 27.11.2017.
 */
public abstract class AgreementEvent extends BaseNeedAndConnectionSpecificEvent {
    private final URI agreementUri;

    public AgreementEvent(Connection con, URI agreementUri) {
        super(con);
        this.agreementUri = agreementUri;
    }

    public URI getAgreementUri() {
        return agreementUri;
    }
}
