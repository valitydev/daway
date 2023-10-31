package dev.vality.daway.handler.event.stock.impl.partymngmnt.contract;

import dev.vality.damsel.domain.PayoutTool;
import dev.vality.damsel.payment_processing.ClaimEffect;
import dev.vality.damsel.payment_processing.ContractEffectUnit;
import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.daway.dao.party.iface.ContractDao;
import dev.vality.daway.dao.party.iface.PayoutToolDao;
import dev.vality.daway.domain.tables.pojos.Contract;
import dev.vality.daway.factory.claim.effect.ClaimEffectCopyFactory;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.AbstractClaimChangedHandler;
import dev.vality.daway.service.ContractReferenceService;
import dev.vality.daway.util.ContractUtil;
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
public class ContractPayoutToolCreatedHandler extends AbstractClaimChangedHandler {

    private final ContractDao contractDao;
    private final PayoutToolDao payoutToolDao;
    private final ContractReferenceService contractReferenceService;
    private final ClaimEffectCopyFactory<Contract, Integer> claimEffectCopyFactory;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        List<ClaimEffect> claimEffects = getClaimStatus(change).getAccepted().getEffects();
        for (int i = 0; i < claimEffects.size(); i++) {
            ClaimEffect claimEffect = claimEffects.get(i);
            if (claimEffect.isSetContractEffect()
                    && claimEffect.getContractEffect().getEffect().isSetPayoutToolCreated()) {
                handleEvent(event, changeId, sequenceId, claimEffects.get(i), i);
            }
        }
    }

    private void handleEvent(MachineEvent event, Integer changeId, long sequenceId, ClaimEffect claimEffect,
                             Integer claimEffectId) {
        ContractEffectUnit contractEffectUnit = claimEffect.getContractEffect();
        PayoutTool payoutToolCreated = contractEffectUnit.getEffect().getPayoutToolCreated();
        String contractId = contractEffectUnit.getContractId();
        String partyId = event.getSourceId();
        log.info("Start contract payout tool created handling, sequenceId={}, partyId={}, contractId={}, changeId={}",
                sequenceId, partyId, contractId, changeId);
        Contract contractSourceOld = contractDao.get(partyId, contractId);
        Contract contractNew =
                claimEffectCopyFactory.create(event, sequenceId, claimEffectId, changeId, contractSourceOld);

        contractDao.save(contractNew).ifPresentOrElse(
                dbContractId -> {
                    Long oldId = contractSourceOld.getId();
                    contractDao.updateNotCurrent(oldId);
                    contractReferenceService.updateContractReference(oldId, dbContractId);
                    payoutToolDao.save(List.of(ContractUtil.convertPayoutTool(payoutToolCreated, dbContractId)));
                    log.info("Contract contract payout tool created has been saved, " +
                                    "sequenceId={}, partyId={}, contractId={}, changeId={}",
                            sequenceId, partyId, contractId, changeId);
                },
                () -> log.info("Contract contract payout tool created duplicated, " +
                                "sequenceId={}, partyId={}, contractId={}, changeId={}",
                        sequenceId, partyId, contractId, changeId)
        );
    }
}
