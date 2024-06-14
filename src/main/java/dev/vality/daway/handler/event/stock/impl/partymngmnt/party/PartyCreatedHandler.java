package dev.vality.daway.handler.event.stock.impl.partymngmnt.party;

import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.damsel.payment_processing.PartyCreated;
import dev.vality.daway.dao.party.iface.PartyDao;
import dev.vality.daway.domain.enums.Blocking;
import dev.vality.daway.domain.enums.Suspension;
import dev.vality.daway.domain.tables.pojos.Party;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.PartyManagementHandler;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyCreatedHandler implements PartyManagementHandler {

    private final PartyDao partyDao;
    private final MachineEventCopyFactory<Party, Integer> partyIntegerMachineEventCopyFactory;

    @Getter
    private final Filter filter =
            new PathConditionFilter(new PathConditionRule("party_created", new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        PartyCreated partyCreated = change.getPartyCreated();
        String partyId = partyCreated.getId();
        log.info("Start party created handling, sequenceId={}, partyId={}, changeId={}", sequenceId, partyId, changeId);

        Party party = partyIntegerMachineEventCopyFactory.create(event, sequenceId, changeId, null);
        party.setPartyId(partyId);
        party.setName(partyCreated.getPartyName());
        party.setContactInfoEmail(partyCreated.getContactInfo().getRegistrationEmail());
        List<String> managerContactEmails = partyCreated.getContactInfo().getManagerContactEmails();
        party.setManagerContactEmails(StringUtils.collectionToDelimitedString(managerContactEmails, ","));
        LocalDateTime partyCreatedAt = TypeUtil.stringToLocalDateTime(partyCreated.getCreatedAt());
        party.setCreatedAt(partyCreatedAt);
        party.setBlocking(Blocking.unblocked);
        party.setBlockingUnblockedReason("");
        party.setBlockingUnblockedSince(partyCreatedAt);
        party.setSuspension(Suspension.active);
        party.setSuspensionActiveSince(partyCreatedAt);
        party.setRevision(0L);
        party.setRevisionChangedAt(partyCreatedAt);
        party.setComment(partyCreated.getComment());
        partyDao.save(party).ifPresentOrElse(
                atLong -> log.info("Party has been saved, sequenceId={}, partyId={}, changeId={}", sequenceId, partyId,
                        changeId),
                () -> log.info("Party create duplicated, sequenceId={}, partyId={}, changeId={}", sequenceId, partyId,
                        changeId));
    }

}
