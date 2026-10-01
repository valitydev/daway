package dev.vality.daway.handler.event.stock.impl.destination;

import dev.vality.daway.integration.base.AbstractPostgresqlIntegrationTest;
import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.domain.tables.pojos.Destination;
import dev.vality.daway.utils.DestinationHandlerTestUtils;
import dev.vality.mapper.RecordRowMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static dev.vality.daway.domain.tables.Destination.DESTINATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@PostgresqlSpringBootITest
class DestinationCreatedGenericSBPTransferHandlerTest extends AbstractPostgresqlIntegrationTest {

    @Autowired
    private DestinationCreatedHandler destinationCreatedHandler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    String sqlStatement = "select * from dw.destination where destination_id = ?;";

    @Test
    void destinationCreatedHandlerTest() {
        String destinationId = UUID.randomUUID().toString();
        dev.vality.fistful.base.Resource fistfulResource = new dev.vality.fistful.base.Resource();
        fistfulResource.setGeneric(DestinationHandlerTestUtils.createResourceGenericSBPTransfer());
        dev.vality.fistful.destination.Destination fistfulDestination
                = DestinationHandlerTestUtils.createFistfulDestination(fistfulResource);
        fistfulDestination.setId(destinationId);

        destinationCreatedHandler.handle(
                DestinationHandlerTestUtils.createCreated(fistfulDestination),
                DestinationHandlerTestUtils.createCreatedMachineEvent(
                        destinationId,
                        fistfulDestination
                ));

        Destination destinationResult = jdbcTemplate.queryForObject(sqlStatement,
                new RecordRowMapper<>(DESTINATION, Destination.class), destinationId);

        Assertions.assertNotNull(destinationResult.getResourceGenericData());
        assertThat(destinationResult.getResourceGenericData(),
                containsString(DestinationHandlerTestUtils.PHONE_NUMBER));
    }

}
