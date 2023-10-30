package dev.vality.daway.handler.event.stock.impl.partymngmnt.contractor;

import dev.vality.damsel.domain.ContractorIdentificationLevel;
import dev.vality.damsel.payment_processing.ClaimEffect;
import dev.vality.damsel.payment_processing.ContractorEffectUnit;
import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.daway.dao.party.iface.ContractorDao;
import dev.vality.daway.domain.tables.pojos.Contractor;
import dev.vality.daway.factory.claim.effect.ClaimEffectCopyFactory;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.AbstractClaimChangedHandler;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractorIdentificationalLevelChangedHandler extends AbstractClaimChangedHandler {

    private final ContractorDao contractorDao;
    private final ClaimEffectCopyFactory<Contractor, Integer> claimEffectCopyFactory;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        List<ClaimEffect> claimEffects = getClaimStatus(change).getAccepted().getEffects();
        for (int i = 0; i < claimEffects.size(); i++) {
            ClaimEffect claimEffect = claimEffects.get(i);
            if (claimEffect.isSetContractorEffect()
                    && claimEffect.getContractorEffect().getEffect().isSetIdentificationLevelChanged()) {
                handleEvent(event, changeId, sequenceId, claimEffects.get(i), i);
            }
        }
    }

    private void handleEvent(MachineEvent event, Integer changeId, long sequenceId, ClaimEffect claimEffect,
                             Integer claimEffectId) {
        ContractorEffectUnit contractorEffect = claimEffect.getContractorEffect();
        ContractorIdentificationLevel identificationLevelChanged =
                contractorEffect.getEffect().getIdentificationLevelChanged();
        String contractorId = contractorEffect.getId();
        String partyId = event.getSourceId();
        log.info("Start identificational level changed handling, sequenceId={}, partyId={}, contractorId={}",
                sequenceId, partyId, contractorId);
        Contractor contractorOld = contractorDao.get(partyId, contractorId);
        Contractor contractorNew =
                claimEffectCopyFactory.create(event, sequenceId, claimEffectId, changeId, contractorOld);

        contractorNew.setIdentificationalLevel(identificationLevelChanged.name());

        contractorDao.save(contractorNew)
                .ifPresentOrElse(
                        saveResult -> {
                            contractorDao.updateNotCurrent(contractorOld.getId());
                            log.info("Contractor identificational has been saved, " +
                                    "sequenceId={}, partyId={}, changeId={}", sequenceId, partyId, changeId);
                        },
                        () -> log.info("Contractor identificational duplicated, " +
                                "sequenceId={}, partyId={}, changeId={}", sequenceId, partyId, changeId)
                );
    }
}
