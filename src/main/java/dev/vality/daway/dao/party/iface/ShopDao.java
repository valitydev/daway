package dev.vality.daway.dao.party.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.Shop;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;

public interface ShopDao extends GenericDao {

    Optional<Long> save(Shop shop) throws DaoException;

    Shop get(String partyId, String shopId) throws DaoException;

    void updateNotCurrent(Long id) throws DaoException;

    void saveWithUpdateCurrent(Shop shopSource, Long oldEventId, String eventName);
}
