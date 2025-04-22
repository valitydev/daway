package dev.vality.daway.dao.withdrawal.session.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.withdrawal.session.iface.WithdrawalSessionDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalSession;
import dev.vality.daway.domain.tables.records.WithdrawalSessionRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static dev.vality.daway.domain.tables.WithdrawalSession.WITHDRAWAL_SESSION;

@Component
public class WithdrawalSessionDaoImpl extends AbstractGenericDao implements WithdrawalSessionDao {

    private final RowMapper<WithdrawalSession> withdrawalSessionRowMapper;

    @Autowired
    public WithdrawalSessionDaoImpl(@Qualifier("dataSource") DataSource dataSource) {
        super(dataSource);
        withdrawalSessionRowMapper = new RecordRowMapper<>(WITHDRAWAL_SESSION, WithdrawalSession.class);
    }

    @Override
    public Optional<Long> save(WithdrawalSession withdrawalSession) throws DaoException {
        WithdrawalSessionRecord record = getDslContext().newRecord(WITHDRAWAL_SESSION, withdrawalSession);
        Query query = getDslContext()
                .insertInto(WITHDRAWAL_SESSION)
                .set(record)
                .onConflict(
                        WITHDRAWAL_SESSION.WITHDRAWAL_SESSION_ID,
                        WITHDRAWAL_SESSION.SEQUENCE_ID,
                        WITHDRAWAL_SESSION.EVENT_CREATED_AT)
                .doNothing()
                .returning(WITHDRAWAL_SESSION.ID);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public WithdrawalSession get(String sessionId) throws DaoException {
        Query query = getDslContext().selectFrom(WITHDRAWAL_SESSION)
                .where(WITHDRAWAL_SESSION.WITHDRAWAL_SESSION_ID.eq(sessionId)
                        .and(WITHDRAWAL_SESSION.CURRENT));
        return Optional.ofNullable(fetchOne(query, withdrawalSessionRowMapper))
                .orElseThrow(() -> new NotFoundException(
                        String.format("WithdrawalSession not found, sessionId='%s'", sessionId)));
    }

    @Override
    public void updateNotCurrent(Long id) throws DaoException {
        Query query = getDslContext().update(WITHDRAWAL_SESSION).set(WITHDRAWAL_SESSION.CURRENT, false)
                .where(WITHDRAWAL_SESSION.ID.eq(id)
                        .and(WITHDRAWAL_SESSION.CURRENT));
        execute(query);
    }
}
