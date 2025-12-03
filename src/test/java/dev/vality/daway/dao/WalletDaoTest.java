package dev.vality.daway.dao;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.dominant.impl.WalletDaoImpl;
import dev.vality.daway.domain.tables.pojos.Wallet;
import dev.vality.daway.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertThrows;

@PostgresqlSpringBootITest
public class WalletDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WalletDaoImpl walletDao;

    @Test
    public void walletDaoTest() {
        jdbcTemplate.execute("truncate table dw.wallet cascade");
        Wallet wallet = dev.vality.testcontainers.annotations.util.RandomBeans.random(Wallet.class);
        wallet.setCurrent(true);
        Long id = walletDao.save(wallet);
        wallet.setId(id);
        Wallet actual = walletDao.get(wallet.getWalletId());
        Assertions.assertEquals(wallet, actual);
        walletDao.updateNotCurrent(actual.getWalletId());

        //check duplicate not error
        walletDao.save(wallet);

        assertThrows(NotFoundException.class, () -> walletDao.get(wallet.getWalletId()));
    }

}