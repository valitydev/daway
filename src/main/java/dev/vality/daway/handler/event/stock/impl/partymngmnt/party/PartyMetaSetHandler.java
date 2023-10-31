package dev.vality.daway.handler.event.stock.impl.partymngmnt.party;

import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.damsel.payment_processing.PartyMetaSet;
import dev.vality.daway.dao.party.iface.PartyDao;
import dev.vality.daway.domain.tables.pojos.Party;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.PartyManagementHandler;
import dev.vality.daway.util.JsonUtil;
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
public class PartyMetaSetHandler implements PartyManagementHandler {

    private final PartyDao partyDao;
    private final MachineEventCopyFactory<Party, Integer> partyIntegerMachineEventCopyFactory;

    @Getter
    private final Filter filter =
            new PathConditionFilter(new PathConditionRule("party_meta_set", new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        PartyMetaSet partyMetaSet = change.getPartyMetaSet();
        String partyId = event.getSourceId();
        log.info("Start party metaset handling, sequenceId={}, partyId={}, changeId={}", sequenceId, partyId, changeId);

        Party partyOld = partyDao.get(partyId);
        Party partyNew = partyIntegerMachineEventCopyFactory.create(event, sequenceId, changeId, partyOld, null);

        partyNew.setPartyMetaSetNs(partyMetaSet.getNs());
        partyNew.setPartyMetaSetDataJson(JsonUtil.thriftBaseToJsonString(partyMetaSet.getData()));

        partyDao.saveWithUpdateCurrent(partyNew, partyOld.getId(), "metaset");
    }

}
