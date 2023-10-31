package dev.vality.daway.dao;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.destination.iface.DestinationDao;
import dev.vality.daway.domain.tables.pojos.Destination;
import dev.vality.daway.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertThrows;

@PostgresqlSpringBootITest
public class DestinationDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DestinationDao destinationDao;

    @Test
    public void destinationDaoTest() {
        jdbcTemplate.execute("truncate table dw.destination cascade");
        Destination destination = dev.vality.testcontainers.annotations.util.RandomBeans.random(Destination.class);
        destination.setCurrent(true);
        Long id = destinationDao.save(destination).get();
        destination.setId(id);
        Destination actual = destinationDao.get(destination.getDestinationId());
        Assertions.assertEquals(destination, actual);
        destinationDao.updateNotCurrent(actual.getId());

        //check duplicate not error
        destinationDao.save(destination);

        assertThrows(NotFoundException.class, () -> destinationDao.get(destination.getDestinationId()));
    }

}
