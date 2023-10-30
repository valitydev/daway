package dev.vality.daway.dao.party.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.ContractAdjustment;
import dev.vality.daway.exception.DaoException;

import java.util.List;

public interface ContractAdjustmentDao extends GenericDao {
    void save(List<ContractAdjustment> contractAdjustmentList) throws DaoException;

    List<ContractAdjustment> getByCntrctId(Long cntrctId) throws DaoException;
}
