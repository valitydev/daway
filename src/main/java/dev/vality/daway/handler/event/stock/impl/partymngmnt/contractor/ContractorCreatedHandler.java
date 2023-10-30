package dev.vality.daway.handler.event.stock.impl.partymngmnt.contractor;

import dev.vality.damsel.domain.PartyContractor;
import dev.vality.damsel.payment_processing.ClaimEffect;
import dev.vality.damsel.payment_processing.ContractorEffectUnit;
import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.daway.dao.party.iface.ContractorDao;
import dev.vality.daway.dao.party.iface.PartyDao;
import dev.vality.daway.domain.tables.pojos.Contractor;
import dev.vality.daway.factory.contractor.ContractorFactory;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.AbstractClaimChangedHandler;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Slf4j
@Component
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ContractorCreatedHandler extends AbstractClaimChangedHandler {

    private final ContractorDao contractorDao;
    private final PartyDao partyDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        long eventId = event.getEventId();
        long sequenceId = event.getEventId();
        List<ClaimEffect> claimEffects = getClaimStatus(change).getAccepted().getEffects();
        for (int i = 0; i < claimEffects.size(); i++) {
            ClaimEffect claimEffect = claimEffects.get(i);
            if (claimEffect.isSetContractorEffect() && claimEffect.getContractorEffect().getEffect().isSetCreated()) {
                handleEvent(event, changeId, eventId, sequenceId, claimEffects.get(i), i);
            }
        }
    }

    private void handleEvent(MachineEvent event, Integer changeId, long eventId, long sequenceId,
                             ClaimEffect claimEffect, Integer claimEffectId) {
        ContractorEffectUnit contractorEffect = claimEffect.getContractorEffect();
        PartyContractor partyContractor = contractorEffect.getEffect().getCreated();
        dev.vality.damsel.domain.Contractor contractorCreated = partyContractor.getContractor();
        String contractorId = contractorEffect.getId();
        String partyId = event.getSourceId();
        log.info("Start contractor created handling, eventId={}, partyId={}, contractorId={}",
                eventId, partyId, contractorId);
        partyDao.get(partyId); //check party is exist

        Contractor contractor = ContractorFactory.build(
                eventId, event.getCreatedAt(), partyId, contractorCreated, contractorId, changeId, claimEffectId);
        contractor.setIdentificationalLevel(partyContractor.getStatus().name());
        contractorDao.save(contractor).ifPresentOrElse(
                cntrctId -> log.info("Contract contractor has been saved, eventId={}, partyId={}, contractorId={}",
                        eventId, partyId, contractorId),
                () -> log.info("contract contractor duplicated, sequenceId={}, partyId={}, changeId={}",
                        sequenceId, partyId, changeId)
        );
    }


}
