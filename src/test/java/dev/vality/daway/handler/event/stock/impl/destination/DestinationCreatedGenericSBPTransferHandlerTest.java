package dev.vality.daway.handler.event.stock.impl.destination;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.domain.tables.pojos.Destination;
import dev.vality.daway.utils.DestinationHandlerTestUtils;
import dev.vality.mapper.RecordRowMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static dev.vality.daway.domain.tables.Destination.DESTINATION;

@PostgresqlSpringBootITest
class DestinationCreatedGenericSBPTransferHandlerTest {

    @Autowired
    private DestinationCreatedHandler destinationCreatedHandler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    Destination destination = dev.vality.testcontainers.annotations.util.RandomBeans.random(Destination.class);
    String sqlStatement = "select * from dw.destination LIMIT 1;";

    @BeforeEach
    public void setUp() {
        destination.setCurrent(true);
    }

    @Test
    void destinationCreatedHandlerTest() {
        dev.vality.fistful.base.Resource fistfulResource = new dev.vality.fistful.base.Resource();
        fistfulResource.setGeneric(DestinationHandlerTestUtils.createResourceGenericSBPTransfer());
        dev.vality.fistful.destination.Destination fistfulDestination
                = DestinationHandlerTestUtils.createFistfulDestination(fistfulResource);

        destinationCreatedHandler.handle(
                DestinationHandlerTestUtils.createCreated(fistfulDestination),
                DestinationHandlerTestUtils.createCreatedMachineEvent(
                        destination.getDestinationId(),
                        fistfulDestination
                ));

        Destination destinationResult = jdbcTemplate.queryForObject(sqlStatement,
                new RecordRowMapper<>(DESTINATION, Destination.class));

        Assertions.assertNotNull(destinationResult.getResourceGenericSbpTransferPhone());
        Assertions.assertEquals(DestinationHandlerTestUtils.PHONE_NUMBER,
                destinationResult.getResourceGenericSbpTransferPhone());
    }

}