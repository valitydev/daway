package dev.vality.daway.dao.deposit.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.deposit.iface.DepositDao;
import dev.vality.daway.domain.tables.pojos.Deposit;
import dev.vality.daway.domain.tables.records.DepositRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static dev.vality.daway.domain.tables.Deposit.DEPOSIT;

@Component
public class DepositDaoImpl extends AbstractGenericDao implements DepositDao {

    private final RowMapper<Deposit> depositRowMapper;

    @Autowired
    public DepositDaoImpl(DataSource dataSource) {
        super(dataSource);
        depositRowMapper = new RecordRowMapper<>(DEPOSIT, Deposit.class);
    }

    @Override
    public Optional<Long> save(Deposit deposit) throws DaoException {
        DepositRecord record = getDslContext().newRecord(DEPOSIT, deposit);
        Query query = getDslContext()
                .insertInto(DEPOSIT)
                .set(record)
                .onConflict(DEPOSIT.DEPOSIT_ID, DEPOSIT.SEQUENCE_ID)
                .doNothing()
                .returning(DEPOSIT.ID);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public Deposit get(String depositId) throws DaoException {
        Query query = getDslContext().selectFrom(DEPOSIT)
                .where(DEPOSIT.DEPOSIT_ID.eq(depositId)
                        .and(DEPOSIT.CURRENT));
        return Optional.ofNullable(fetchOne(query, depositRowMapper))
                .orElseThrow(
                        () -> new NotFoundException(String.format("Deposit not found, depositId='%s'", depositId)));
    }

    @Override
    public void updateNotCurrent(Long depositId) throws DaoException {
        Query query = getDslContext().update(DEPOSIT).set(DEPOSIT.CURRENT, false)
                .where(DEPOSIT.ID.eq(depositId)
                        .and(DEPOSIT.CURRENT));
        execute(query);
    }

}
