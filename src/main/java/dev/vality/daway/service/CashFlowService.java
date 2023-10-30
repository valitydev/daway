package dev.vality.daway.service;

import dev.vality.daway.dao.invoicing.iface.CashFlowDao;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CashFlowService {

    private final CashFlowDao cashFlowDao;

    public void save(Long objSourceId, Long objId, PaymentChangeType type) {
        if (objId != null) {
            List<CashFlow> cashFlows = cashFlowDao.getByObjId(objSourceId, type);
            cashFlows.forEach(pcf -> {
                pcf.setId(null);
                pcf.setObjId(objId);
            });
            cashFlowDao.save(cashFlows);
        }
    }
}
