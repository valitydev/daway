package dev.vality.daway.dao;

import dev.vality.daway.config.PostgresqlJooqSpringBootITest;
import dev.vality.daway.dao.withdrawal.session.iface.WithdrawalSessionDao;
import dev.vality.daway.dao.withdrawal.session.impl.WithdrawalSessionDaoImpl;
import dev.vality.daway.domain.tables.pojos.WithdrawalSession;
import dev.vality.daway.exception.NotFoundException;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import static dev.vality.daway.domain.tables.WithdrawalSession.WITHDRAWAL_SESSION;
import static org.junit.Assert.assertThrows;

@ContextConfiguration(classes = {WithdrawalSessionDaoImpl.class})
@PostgresqlJooqSpringBootITest
@Sql(scripts = {"classpath:sql/partition_idx.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class WithdrawalSessionDaoTest {
    @Autowired
    private DSLContext dslContext;

    @Autowired
    private WithdrawalSessionDao withdrawalSessionDao;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(WITHDRAWAL_SESSION).execute();
    }

    @Test
    void withdrawalSessionDao() {
        WithdrawalSession withdrawalSession = dev.vality.testcontainers.annotations.util.RandomBeans.random(WithdrawalSession.class);
        withdrawalSession.setCurrent(true);
        Long id = withdrawalSessionDao.save(withdrawalSession).get();
        withdrawalSession.setId(id);
        WithdrawalSession actual = withdrawalSessionDao.get(withdrawalSession.getWithdrawalSessionId());
        Assertions.assertEquals(withdrawalSession, actual);
        withdrawalSessionDao.updateNotCurrent(actual.getId());

        //check duplicate not error
        withdrawalSessionDao.save(withdrawalSession);

        assertThrows(NotFoundException.class, () -> withdrawalSessionDao.get(withdrawalSession.getWithdrawalSessionId()));
    }
}
