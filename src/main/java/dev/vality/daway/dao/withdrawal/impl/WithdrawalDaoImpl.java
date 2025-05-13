package dev.vality.daway.dao.withdrawal.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalDao;
import dev.vality.daway.domain.tables.pojos.Withdrawal;
import dev.vality.daway.domain.tables.records.WithdrawalRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Optional;

import static dev.vality.daway.domain.tables.Withdrawal.WITHDRAWAL;

@Component
public class WithdrawalDaoImpl extends AbstractGenericDao implements WithdrawalDao {

    private final RowMapper<Withdrawal> withdrawalRowMapper;

    @Autowired
    public WithdrawalDaoImpl(DataSource dataSource) {
        super(dataSource);
        withdrawalRowMapper = new RecordRowMapper<>(WITHDRAWAL, Withdrawal.class);
    }

    @Override
    public Optional<Long> save(Withdrawal withdrawal) throws DaoException {
        WithdrawalRecord record = getDslContext().newRecord(WITHDRAWAL, withdrawal);
        Query query = getDslContext()
                .insertInto(WITHDRAWAL)
                .set(record)
                .onConflict(
                        WITHDRAWAL.WITHDRAWAL_ID,
                        WITHDRAWAL.SEQUENCE_ID,
                        WITHDRAWAL.EVENT_CREATED_AT)
                .doNothing()
                .returning(WITHDRAWAL.ID);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public Withdrawal get(String withdrawalId) throws DaoException {
        Query query = getDslContext().selectFrom(WITHDRAWAL)
                .where(WITHDRAWAL.WITHDRAWAL_ID.eq(withdrawalId)
                        .and(WITHDRAWAL.CURRENT));
        return Optional.ofNullable(fetchOne(query, withdrawalRowMapper))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Withdrawal not found, withdrawalId='%s'", withdrawalId)));
    }

    @Override
    public Withdrawal get(String withdrawalId, LocalDateTime from, LocalDateTime to) throws DaoException {
        Query query = getDslContext().selectFrom(WITHDRAWAL)
                .where(
                        WITHDRAWAL.EVENT_CREATED_AT.between(from, to)
                                .and(WITHDRAWAL.WITHDRAWAL_ID.eq(withdrawalId))
                                .and(WITHDRAWAL.CURRENT)
                );
        return Optional.ofNullable(fetchOne(query, withdrawalRowMapper))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Withdrawal not found, withdrawalId='%s'", withdrawalId)));
    }

    @Override
    public void updateNotCurrent(Long id) throws DaoException {
        Query query = getDslContext().update(WITHDRAWAL).set(WITHDRAWAL.CURRENT, false)
                .where(WITHDRAWAL.ID.eq(id)
                        .and(WITHDRAWAL.CURRENT));
        execute(query);
    }
}
