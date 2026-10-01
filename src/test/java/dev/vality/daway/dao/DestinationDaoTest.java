package dev.vality.daway.dao;

import dev.vality.daway.integration.base.AbstractPostgresqlIntegrationTest;
import dev.vality.daway.dao.destination.iface.DestinationDao;
import dev.vality.daway.domain.tables.pojos.Destination;
import dev.vality.daway.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static dev.vality.daway.utils.RandomBeans.random;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class DestinationDaoTest extends AbstractPostgresqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DestinationDao destinationDao;

    @Test
    public void destinationDaoTest() {
        jdbcTemplate.execute("truncate table dw.destination cascade");
        Destination destination = random(Destination.class);
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
