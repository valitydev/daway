package dev.vality.daway.dao.withdrawal.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.enums.FistfulCashFlowChangeType;
import dev.vality.daway.domain.tables.pojos.FistfulCashFlow;
import dev.vality.daway.exception.DaoException;

import java.util.List;

public interface FistfulCashFlowDao extends GenericDao {

    void save(List<FistfulCashFlow> fistfulCashFlowList) throws DaoException;

    List<FistfulCashFlow> getByObjId(Long objId, FistfulCashFlowChangeType cashFlowChangeType) throws DaoException;

}
