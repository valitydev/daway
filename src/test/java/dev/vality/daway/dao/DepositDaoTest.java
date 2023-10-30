package dev.vality.daway.dao;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.deposit.iface.DepositDao;
import dev.vality.daway.domain.tables.pojos.Deposit;
import dev.vality.daway.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertThrows;

@PostgresqlSpringBootITest
public class DepositDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DepositDao depositDao;

    @Test
    public void depositDaoTest() {
        jdbcTemplate.execute("truncate table dw.deposit cascade");
        Deposit deposit = dev.vality.testcontainers.annotations.util.RandomBeans.random(Deposit.class);
        deposit.setCurrent(true);
        Long id = depositDao.save(deposit).get();
        deposit.setId(id);
        Deposit actual = depositDao.get(deposit.getDepositId());
        Assertions.assertEquals(deposit, actual);
        depositDao.updateNotCurrent(actual.getId());

        //check duplicate not error
        depositDao.save(deposit);

        assertThrows(NotFoundException.class, () -> depositDao.get(deposit.getDepositId()));
    }

}
