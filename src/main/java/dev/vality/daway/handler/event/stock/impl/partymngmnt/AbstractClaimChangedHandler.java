package dev.vality.daway.handler.event.stock.impl.partymngmnt;

import dev.vality.damsel.payment_processing.ClaimStatus;
import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;

public abstract class AbstractClaimChangedHandler implements PartyManagementHandler {

    private final Filter claimCreatedFilter = new PathConditionFilter(
            new PathConditionRule("claim_created.status.accepted", new IsNullCondition().not()));

    private final Filter claimStatusChangedFilter = new PathConditionFilter(
            new PathConditionRule("claim_status_changed.status.accepted", new IsNullCondition().not()));

    @Override
    public boolean accept(PartyChange change) {
        return claimCreatedFilter.match(change) || claimStatusChangedFilter.match(change);
    }

    @Override
    public Filter<PartyChange> getFilter() {
        return claimCreatedFilter;
    }

    protected ClaimStatus getClaimStatus(PartyChange change) {
        ClaimStatus claimStatus = null;
        if (change.isSetClaimCreated()) {
            claimStatus = change.getClaimCreated().getStatus();
        } else if (change.isSetClaimStatusChanged()) {
            claimStatus = change.getClaimStatusChanged().getStatus();
        }
        return claimStatus;
    }

}
