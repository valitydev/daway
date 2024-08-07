package dev.vality.daway.dao.recurrent.payment.tool.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.recurrent.payment.tool.iface.RecurrentPaymentToolDao;
import dev.vality.daway.domain.tables.pojos.RecurrentPaymentTool;
import dev.vality.daway.domain.tables.records.RecurrentPaymentToolRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static dev.vality.daway.domain.tables.RecurrentPaymentTool.RECURRENT_PAYMENT_TOOL;

@Component
public class RecurrentPaymentToolDaoImpl extends AbstractGenericDao implements RecurrentPaymentToolDao {

    private final RowMapper<RecurrentPaymentTool> recurrentPaymentToolRowMapper;

    public RecurrentPaymentToolDaoImpl(DataSource dataSource) {
        super(dataSource);
        recurrentPaymentToolRowMapper = new RecordRowMapper<>(RECURRENT_PAYMENT_TOOL, RecurrentPaymentTool.class);
    }

    @Override
    public Optional<Long> save(RecurrentPaymentTool source) throws DaoException {
        RecurrentPaymentToolRecord record = getDslContext().newRecord(RECURRENT_PAYMENT_TOOL, source);
        Query query = getDslContext().insertInto(RECURRENT_PAYMENT_TOOL)
                .set(record)
                .onConflict(RECURRENT_PAYMENT_TOOL.RECURRENT_PAYMENT_TOOL_ID,
                        RECURRENT_PAYMENT_TOOL.SEQUENCE_ID,
                        RECURRENT_PAYMENT_TOOL.CHANGE_ID)
                .doNothing()
                .returning(RECURRENT_PAYMENT_TOOL.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public RecurrentPaymentTool get(String recurrentPaymentToolId) throws DaoException {
        Query query = getDslContext().selectFrom(RECURRENT_PAYMENT_TOOL)
                .where(RECURRENT_PAYMENT_TOOL.RECURRENT_PAYMENT_TOOL_ID.eq(recurrentPaymentToolId)
                        .and(RECURRENT_PAYMENT_TOOL.CURRENT));

        return Optional.ofNullable(fetchOne(query, recurrentPaymentToolRowMapper))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Recurrent payment tool not found, sourceId='%s'", recurrentPaymentToolId)));
    }

    @Override
    public void updateNotCurrent(Long id) throws DaoException {
        Query query = getDslContext().update(RECURRENT_PAYMENT_TOOL).set(RECURRENT_PAYMENT_TOOL.CURRENT, false)
                .where(RECURRENT_PAYMENT_TOOL.ID.eq(id));
        executeOne(query);
    }


}

