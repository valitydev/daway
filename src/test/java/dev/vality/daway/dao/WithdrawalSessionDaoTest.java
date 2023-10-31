package dev.vality.daway.dao;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.withdrawal.session.iface.WithdrawalSessionDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalSession;
import dev.vality.daway.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertThrows;

@PostgresqlSpringBootITest
public class WithdrawalSessionDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WithdrawalSessionDao withdrawalSessionDao;

    @Test
    public void withdrawalSessionDao() {
        jdbcTemplate.execute("truncate table dw.withdrawal_session cascade");
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
