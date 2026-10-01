package dev.vality.daway.dao;

import dev.vality.daway.integration.base.AbstractPostgresqlIntegrationTest;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalDao;
import dev.vality.daway.dao.withdrawal.impl.WithdrawalDaoImpl;
import dev.vality.daway.domain.tables.pojos.Withdrawal;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static dev.vality.daway.domain.tables.Withdrawal.WITHDRAWAL;
import static dev.vality.daway.utils.RandomBeans.random;
import static org.junit.jupiter.api.Assertions.assertNull;

@ContextConfiguration(classes = {WithdrawalDaoImpl.class})
@JooqTest
@Sql(scripts = {"classpath:sql/partition_idx.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class WithdrawalDaoTest extends AbstractPostgresqlIntegrationTest {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private WithdrawalDao withdrawalDao;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(WITHDRAWAL).execute();
    }

    @Test
    void withdrawalDaoTest() {
        Withdrawal withdrawal = random(Withdrawal.class);
        withdrawal.setCurrent(true);
        withdrawal.setExchangeRate(new BigDecimal(1000000L).movePointLeft(4));
        Long id = withdrawalDao.save(withdrawal).get();
        withdrawal.setId(id);
        LocalDateTime toTime = withdrawal.getEventCreatedAt();
        String withdrawalId = withdrawal.getWithdrawalId();
        Withdrawal actual = withdrawalDao.get(withdrawalId, toTime.minusMonths(1), toTime);
        Assertions.assertEquals(withdrawal, actual);
        withdrawalDao.updateNotCurrent(actual.getId());

        //check duplicate not error
        withdrawalDao.save(withdrawal);

        assertNull(withdrawalDao.get(withdrawalId, toTime.minusMonths(1), toTime));
    }

}
