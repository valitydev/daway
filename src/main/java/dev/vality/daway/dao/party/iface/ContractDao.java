package dev.vality.daway.dao.party.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.Contract;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface ContractDao extends GenericDao {

    Optional<Long> save(Contract contract) throws DaoException;

    Contract get(String partyId, String contractId) throws DaoException;

    void updateNotCurrent(Long contractId) throws DaoException;
}
