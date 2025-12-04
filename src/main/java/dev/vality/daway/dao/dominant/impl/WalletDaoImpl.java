package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.tables.pojos.Wallet;
import dev.vality.daway.domain.tables.records.WalletRecord;
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

import static dev.vality.daway.domain.tables.Wallet.WALLET;

@Component
public class WalletDaoImpl extends AbstractGenericDao implements DomainObjectDao<Wallet, String> {

    private final RowMapper<Wallet> walletRowMapper;

    @Autowired
    public WalletDaoImpl(DataSource dataSource) {
        super(dataSource);
        walletRowMapper = new RecordRowMapper<>(WALLET, Wallet.class);
    }

    @Override
    public void updateNotCurrent(String walletId) throws DaoException {
        Query query = getDslContext().update(WALLET).set(WALLET.CURRENT, false)
                .where(WALLET.WALLET_ID.eq(walletId)
                        .and(WALLET.CURRENT));
        execute(query);
    }

    @Override
    public Long save(Wallet wallet) throws DaoException {
        WalletRecord record = getDslContext().newRecord(WALLET, wallet);
        Query query = getDslContext()
                .insertInto(WALLET)
                .set(record)
                .onConflict(WALLET.WALLET_ID, WALLET.DOMINANT_VERSION_ID)
                .doNothing()
                .returning(WALLET.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue).orElse(null);
    }

    public Wallet get(String walletId) throws DaoException {
        Query query = getDslContext().selectFrom(WALLET)
                .where(WALLET.WALLET_ID.eq(walletId)
                        .and(WALLET.CURRENT));
        return Optional.ofNullable(fetchOne(query, walletRowMapper))
                .orElseThrow(
                        () -> new NotFoundException(String.format("Wallet not found, walletId='%s'", walletId)));
    }
}