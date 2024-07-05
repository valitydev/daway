package dev.vality.daway.handler.event.stock.impl.partymngmnt.contract;

import dev.vality.damsel.payment_processing.ClaimEffect;
import dev.vality.damsel.payment_processing.ContractEffectUnit;
import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.daway.dao.party.iface.ContractAdjustmentDao;
import dev.vality.daway.dao.party.iface.ContractDao;
import dev.vality.daway.dao.party.iface.ContractorDao;
import dev.vality.daway.dao.party.iface.PartyDao;
import dev.vality.daway.domain.enums.ContractStatus;
import dev.vality.daway.domain.tables.pojos.Contract;
import dev.vality.daway.domain.tables.pojos.ContractAdjustment;
import dev.vality.daway.domain.tables.pojos.Contractor;
import dev.vality.daway.factory.claim.effect.ClaimEffectCopyFactory;
import dev.vality.daway.factory.contractor.ContractorFactory;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.AbstractClaimChangedHandler;
import dev.vality.daway.util.ContractUtil;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Slf4j
@Component
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ContractCreatedHandler extends AbstractClaimChangedHandler {

    private final ContractDao contractDao;
    private final ContractorDao contractorDao;
    private final PartyDao partyDao;
    private final ContractAdjustmentDao contractAdjustmentDao;
    private final ClaimEffectCopyFactory<Contract, Integer> claimEffectCopyFactory;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        List<ClaimEffect> claimEffects = getClaimStatus(change).getAccepted().getEffects();
        for (int i = 0; i < claimEffects.size(); i++) {
            ClaimEffect claimEffect = claimEffects.get(i);
            if (claimEffect.isSetContractEffect() && claimEffect.getContractEffect().getEffect().isSetCreated()) {
                handleEvent(event, changeId, sequenceId, claimEffects.get(i), i);
            }
        }
    }

    private void handleEvent(MachineEvent event, Integer changeId, long sequenceId, ClaimEffect e,
                             Integer claimEffectId) {
        ContractEffectUnit contractEffectUnit = e.getContractEffect();
        String contractId = contractEffectUnit.getContractId();
        String partyId = event.getSourceId();
        log.info("Start contract created handling, sequenceId={}, partyId={}, contractId={}, changeId={}",
                sequenceId, partyId, contractId, changeId);
        Contract contract = claimEffectCopyFactory.create(event, sequenceId, claimEffectId, changeId);
        partyDao.get(partyId); //check party is exist

        contract.setContractId(contractId);
        contract.setPartyId(partyId);
        dev.vality.damsel.domain.Contract contractCreated = contractEffectUnit.getEffect().getCreated();
        if (contractCreated.isSetPaymentInstitution()) {
            contract.setPaymentInstitutionId(contractCreated.getPaymentInstitution().getId());
        }
        contract.setCreatedAt(TypeUtil.stringToLocalDateTime(contractCreated.getCreatedAt()));
        if (contractCreated.isSetValidSince()) {
            contract.setValidSince(TypeUtil.stringToLocalDateTime(contractCreated.getValidSince()));
        }
        if (contractCreated.isSetValidUntil()) {
            contract.setValidUntil(TypeUtil.stringToLocalDateTime(contractCreated.getValidUntil()));
        }
        contract.setStatus(TBaseUtil.unionFieldToEnum(contractCreated.getStatus(), ContractStatus.class));
        if (contractCreated.getStatus().isSetTerminated()) {
            contract.setStatusTerminatedAt(
                    TypeUtil.stringToLocalDateTime(contractCreated.getStatus().getTerminated().getTerminatedAt()));
        }
        contract.setTermsId(contractCreated.getTerms().getId());
        if (contractCreated.isSetLegalAgreement()) {
            ContractUtil.fillContractLegalAgreementFields(contract, contractCreated.getLegalAgreement());
        }
        if (contractCreated.isSetReportPreferences()
                && contractCreated.getReportPreferences().isSetServiceAcceptanceActPreferences()) {
            ContractUtil.fillReportPreferences(contract,
                    contractCreated.getReportPreferences().getServiceAcceptanceActPreferences());
        }

        String contractorId = initContractorId(contractCreated);
        contract.setContractorId(contractorId);

        contractDao.save(contract).ifPresentOrElse(
                cntrctId -> updateContractReference(event, changeId, sequenceId, contractCreated, contractId, partyId,
                        contractorId, cntrctId, claimEffectId),
                () -> log.info("contract create duplicated, sequenceId={}, partyId={}, changeId={}",
                        sequenceId, partyId, changeId)
        );
    }

    private String initContractorId(dev.vality.damsel.domain.Contract contractCreated) {
        String contractorId = "";
        if (contractCreated.isSetContractorId()) {
            contractorId = contractCreated.getContractorId();
        } else if (contractCreated.isSetContractor()) {
            contractorId = UUID.randomUUID().toString();
        }
        return contractorId;
    }

    private void updateContractReference(MachineEvent event, Integer changeId, long sequenceId,
                                         dev.vality.damsel.domain.Contract contractCreated,
                                         String contractId, String partyId, String contractorId, Long cntrctId,
                                         Integer claimEffectId) {
        if (contractCreated.isSetContractor()) {
            Contractor contractor = ContractorFactory.build(sequenceId, event.getCreatedAt(),
                    partyId, contractCreated.getContractor(), contractorId, changeId, claimEffectId);
            contractorDao.save(contractor);
        }

        List<ContractAdjustment> adjustments = ContractUtil.convertContractAdjustments(contractCreated, cntrctId);
        contractAdjustmentDao.save(adjustments);

        log.info("Contract has been saved, sequenceId={}, partyId={}, contractId={}, changeId={}",
                sequenceId, partyId, contractId, changeId);
    }
}
