package dev.vality.daway.dao;

import dev.vality.daway.integration.base.AbstractPostgresqlIntegrationTest;
import dev.vality.daway.dao.deposit.revert.iface.DepositRevertDao;
import dev.vality.daway.domain.tables.pojos.DepositRevert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static dev.vality.daway.utils.RandomBeans.random;

@SpringBootTest
public class DepositRevertDaoTest extends AbstractPostgresqlIntegrationTest {

    @Autowired
    private DepositRevertDao depositRevertDao;

    @Test
    public void depositRevertTest() {
        DepositRevert deposit = random(DepositRevert.class);
        deposit.setCurrent(true);
        Long id = depositRevertDao.save(deposit).get();
        deposit.setId(id);
        DepositRevert actual = depositRevertDao.get(deposit.getDepositId(), deposit.getRevertId());
        Assertions.assertEquals(deposit, actual);
        depositRevertDao.updateNotCurrent(actual.getId());
        Assertions.assertNull(depositRevertDao.get(deposit.getDepositId(), deposit.getRevertId()));

        //check duplicate not error
        depositRevertDao.save(deposit);
    }

}
