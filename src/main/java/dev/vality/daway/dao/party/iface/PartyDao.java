package dev.vality.daway.dao.party.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.Party;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface PartyDao extends GenericDao {
    Optional<Long> save(Party party) throws DaoException;

    Party get(String partyId) throws DaoException;

    void updateNotCurrent(Long partyId) throws DaoException;

    void saveWithUpdateCurrent(Party partySource, Long oldId, String eventName);
}
