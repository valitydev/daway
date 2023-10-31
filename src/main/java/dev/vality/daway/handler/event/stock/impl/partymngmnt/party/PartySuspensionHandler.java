package dev.vality.daway.handler.event.stock.impl.partymngmnt.party;

import dev.vality.damsel.domain.Suspension;
import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.daway.dao.party.iface.PartyDao;
import dev.vality.daway.domain.tables.pojos.Party;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.PartyManagementHandler;
import dev.vality.geck.common.util.TBaseUtil;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class PartySuspensionHandler implements PartyManagementHandler {

    public static final String PARTY_SUSPENSION = "party_suspension";

    private final PartyDao partyDao;
    private final MachineEventCopyFactory<Party, Integer> partyIntegerMachineEventCopyFactory;

    @Getter
    private final Filter filter =
            new PathConditionFilter(new PathConditionRule(PARTY_SUSPENSION, new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        Suspension partySuspension = change.getPartySuspension();
        String partyId = event.getSourceId();
        log.info("Start {} handling, eventId={}, partyId={}, changeId={}", PARTY_SUSPENSION, sequenceId, partyId,
                changeId);
        Party partyOld = partyDao.get(partyId);
        Party partyNew = partyIntegerMachineEventCopyFactory.create(event, sequenceId, changeId, partyOld, null);

        partyNew.setSuspension(
                TBaseUtil.unionFieldToEnum(partySuspension, dev.vality.daway.domain.enums.Suspension.class));
        if (partySuspension.isSetActive()) {
            partyNew.setSuspensionActiveSince(TypeUtil.stringToLocalDateTime(partySuspension.getActive().getSince()));
            partyNew.setSuspensionSuspendedSince(null);
        } else if (partySuspension.isSetSuspended()) {
            partyNew.setSuspensionActiveSince(null);
            partyNew.setSuspensionSuspendedSince(
                    TypeUtil.stringToLocalDateTime(partySuspension.getSuspended().getSince()));
        }

        partyDao.saveWithUpdateCurrent(partyNew, partyOld.getId(), PARTY_SUSPENSION);
    }

}
