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

import java.util.Objects;
import java.util.UUID;

import static dev.vality.daway.domain.tables.Destination.DESTINATION;

@PostgresqlSpringBootITest
public class DestinationCreatedCryptoWalletHandlerTest extends AbstractPostgresqlIntegrationTest {

    @Autowired
    private DestinationCreatedHandler destinationCreatedHandler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    String sqlStatement = "select * from dw.destination where destination_id = ?;";

    @Test
    public void destinationCreatedHandlerTest() {
        String destinationId = UUID.randomUUID().toString();
        dev.vality.fistful.base.Resource fistfulResource = new dev.vality.fistful.base.Resource();
        fistfulResource.setCryptoWallet(DestinationHandlerTestUtils.createResourceCryptoWallet());
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

        Assertions.assertNotNull(Objects.requireNonNull(destinationResult).getResourceCryptoWalletId());
        Assertions.assertNotNull(destinationResult.getResourceCryptoWalletType());
    }

}
