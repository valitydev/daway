package dev.vality.daway.dao.invoicing.iface;

import dev.vality.dao.GenericDao;
import dev.vality.daway.domain.tables.pojos.InvoiceStatusInfo;
import dev.vality.daway.exception.DaoException;

import java.util.List;
import java.util.Set;

public interface InvoiceStatusInfoDao extends GenericDao {

    void saveBatch(List<InvoiceStatusInfo> statuses) throws DaoException;

    InvoiceStatusInfo get(String invoiceId);

    void switchCurrent(Set<String> invoiceIds) throws DaoException;

}
