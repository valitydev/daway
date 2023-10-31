package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.enums.AdjustmentCashFlowType;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import dev.vality.daway.exception.DaoException;

import java.util.List;

public interface CashFlowDao extends GenericDao {

    void save(List<CashFlow> cashFlows) throws DaoException;

    List<CashFlow> getByObjId(Long objId, PaymentChangeType paymentchangetype) throws DaoException;

    List<CashFlow> getForAdjustments(Long adjId, AdjustmentCashFlowType adjustmentcashflowtype) throws DaoException;

}
