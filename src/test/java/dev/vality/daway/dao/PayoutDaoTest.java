package dev.vality.daway.dao;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.payout.iface.PayoutDao;
import dev.vality.daway.domain.tables.pojos.Payout;
import dev.vality.daway.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.Assert.assertThrows;

@PostgresqlSpringBootITest
public class PayoutDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PayoutDao payoutDao;

    @Test
    public void payoutDaoTest() {
        jdbcTemplate.execute("truncate table dw.payout cascade");
        Payout payout = dev.vality.testcontainers.annotations.util.RandomBeans.random(Payout.class);
        payout.setCurrent(true);
        Optional<Long> save = payoutDao.save(payout);
        Payout payoutGet = payoutDao.get(payout.getPayoutId());
        Assertions.assertEquals(payout, payoutGet);
        payoutDao.updateNotCurrent(save.get());

        //check duplicate not error
        payoutDao.save(payout);

        assertThrows(NotFoundException.class, () -> payoutDao.get(payout.getPayoutId()));
    }
}
