package dev.vality.daway.dao.rate.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.Rate;
import dev.vality.daway.exception.DaoException;

import java.util.List;

public interface RateDao extends GenericDao {

    Long save(Rate rate) throws DaoException;

    List<Long> getIds(String sourceId) throws DaoException;

    void updateNotCurrent(List<Long> ids) throws DaoException;

}
