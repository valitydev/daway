package dev.vality.daway.dao.recurrent.payment.tool.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.RecurrentPaymentTool;
import dev.vality.daway.exception.DaoException;

import java.util.Optional;


public interface RecurrentPaymentToolDao extends GenericDao {

    Optional<Long> save(RecurrentPaymentTool source) throws DaoException;

    RecurrentPaymentTool get(String recurrentPaymentToolId) throws DaoException;

    void updateNotCurrent(Long rptId) throws DaoException;

}
