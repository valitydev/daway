package dev.vality.daway.service;

import dev.vality.daway.dao.party.iface.ContractAdjustmentDao;
import dev.vality.daway.domain.tables.pojos.ContractAdjustment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContractReferenceService {

    private final ContractAdjustmentDao contractAdjustmentDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateContractReference(Long contractSourceId, Long contractId) {
        updateAdjustments(contractSourceId, contractId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateAdjustments(Long contractSourceId, Long contractId) {
        List<ContractAdjustment> adjustments = contractAdjustmentDao.getByCntrctId(contractSourceId);
        adjustments.forEach(a -> {
            a.setId(null);
            a.setCntrctId(contractId);
        });
        contractAdjustmentDao.save(adjustments);
    }

}
