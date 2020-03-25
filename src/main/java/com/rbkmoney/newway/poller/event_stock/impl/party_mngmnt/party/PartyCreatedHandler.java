package com.rbkmoney.newway.poller.event_stock.impl.party_mngmnt.party;

import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.damsel.payment_processing.PartyCreated;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.newway.dao.party.iface.PartyDao;
import com.rbkmoney.newway.domain.enums.Blocking;
import com.rbkmoney.newway.domain.enums.Suspension;
import com.rbkmoney.newway.domain.tables.pojos.Party;
import com.rbkmoney.newway.poller.event_stock.impl.party_mngmnt.AbstractPartyManagementHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyCreatedHandler extends AbstractPartyManagementHandler {

    private final PartyDao partyDao;
    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "party_created",
            new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        PartyCreated partyCreated = change.getPartyCreated();
        String partyId = partyCreated.getId();
        log.info("Start party created handling, sequenceId={}, partyId={}, changeId={}", sequenceId, partyId, changeId);
        Party party = new Party();
        party.setSequenceId(sequenceId);
        party.setChangeId(changeId);
        party.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        party.setPartyId(partyId);
        party.setContactInfoEmail(partyCreated.getContactInfo().getEmail());
        LocalDateTime partyCreatedAt = TypeUtil.stringToLocalDateTime(partyCreated.getCreatedAt());
        party.setCreatedAt(partyCreatedAt);
        party.setBlocking(Blocking.unblocked);
        party.setBlockingUnblockedReason("");
        party.setBlockingUnblockedSince(partyCreatedAt);
        party.setSuspension(Suspension.active);
        party.setSuspensionActiveSince(partyCreatedAt);
        party.setRevision(0L);
        party.setRevisionChangedAt(partyCreatedAt);

        partyDao.save(party).ifPresentOrElse(
                aLong -> log.info("Party has been saved, sequenceId={}, partyId={}, changeId={}", sequenceId, partyId, changeId),
                () -> log.info("Party create duplicated, sequenceId={}, partyId={}, changeId={}", sequenceId, partyId, changeId));
    }

    @Override
    public Filter<PartyChange> getFilter() {
        return filter;
    }
}
