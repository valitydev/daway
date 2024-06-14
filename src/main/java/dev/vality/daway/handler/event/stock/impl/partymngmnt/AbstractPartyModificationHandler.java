package dev.vality.daway.handler.event.stock.impl.partymngmnt;

import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.damsel.payment_processing.PartyModification;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;

import java.util.List;

public abstract class AbstractPartyModificationHandler implements PartyManagementHandler {

    private final Filter partyMidificationFilter = new PathConditionFilter(
            new PathConditionRule("claim_created.changeset", new IsNullCondition().not()));

    @Override
    public boolean accept(PartyChange change) {
        return partyMidificationFilter.match(change);
    }

    @Override
    public Filter<PartyChange> getFilter() {
        return partyMidificationFilter;
    }

    protected List<PartyModification> getPartyModification(PartyChange change) {
        List<PartyModification> partyModifications = null;
        if (change.isSetClaimCreated() && change.getClaimCreated().isSetChangeset()) {
            partyModifications = change.getClaimCreated().getChangeset();
        }
        return partyModifications;
    }

}
