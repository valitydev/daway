package dev.vality.daway.dao.invoicing.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.invoicing.iface.InvoiceStatusInfoDao;
import dev.vality.daway.domain.tables.pojos.InvoiceStatusInfo;
import dev.vality.daway.domain.tables.records.InvoiceStatusInfoRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.vality.daway.domain.Tables.INVOICE_STATUS_INFO;

@Component
public class InvoiceStatusInfoDaoImpl extends AbstractGenericDao implements InvoiceStatusInfoDao {

    private final RowMapper<InvoiceStatusInfo> rowMapper;

    public InvoiceStatusInfoDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.rowMapper = new RecordRowMapper<>(INVOICE_STATUS_INFO, InvoiceStatusInfo.class);
    }

    @Override
    public void saveBatch(List<InvoiceStatusInfo> statuses) throws DaoException {
        batchExecute(statuses.stream()
                .map(status -> getDslContext().newRecord(INVOICE_STATUS_INFO, status))
                .map(this::prepareInsertQuery)
                .collect(Collectors.toList())
        );
    }

    @Override
    public InvoiceStatusInfo get(String invoiceId) {
        Query query = getDslContext().selectFrom(INVOICE_STATUS_INFO)
                .where(INVOICE_STATUS_INFO.INVOICE_ID.eq(invoiceId)
                        .and(INVOICE_STATUS_INFO.CURRENT));
        return Optional.ofNullable(fetchOne(query, rowMapper)).orElseThrow(() ->
                new NotFoundException(String.format("InvoiceStatusInfo not found, invoiceId='%s'", invoiceId)));
    }

    @Override
    public void switchCurrent(Set<String> invoiceIds) throws DaoException {
        invoiceIds.forEach(invoiceId -> {
            setOldStatusInfoNotCurrent(invoiceId);
            setLatestStatusInfoCurrent(invoiceId);
        });
    }

    private Query prepareInsertQuery(InvoiceStatusInfoRecord invoiceStatusInfoRecord) {
        return getDslContext().insertInto(INVOICE_STATUS_INFO)
                .set(invoiceStatusInfoRecord)
                .onConflict(
                        INVOICE_STATUS_INFO.INVOICE_ID,
                        INVOICE_STATUS_INFO.SEQUENCE_ID,
                        INVOICE_STATUS_INFO.CHANGE_ID)
                .doNothing();
    }

    private void setOldStatusInfoNotCurrent(String invoiceId) {
        execute(getDslContext().update(INVOICE_STATUS_INFO)
                .set(INVOICE_STATUS_INFO.CURRENT, false)
                .where(INVOICE_STATUS_INFO.INVOICE_ID.eq(invoiceId)
                        .and(INVOICE_STATUS_INFO.CURRENT))
        );
    }

    private void setLatestStatusInfoCurrent(String invoiceId) {
        execute(getDslContext().update(INVOICE_STATUS_INFO)
                .set(INVOICE_STATUS_INFO.CURRENT, true)
                .where(INVOICE_STATUS_INFO.ID.eq(
                        DSL.select(DSL.max(INVOICE_STATUS_INFO.ID))
                                .from(INVOICE_STATUS_INFO)
                                .where(INVOICE_STATUS_INFO.INVOICE_ID.eq(invoiceId))
                ))
        );
    }
}
