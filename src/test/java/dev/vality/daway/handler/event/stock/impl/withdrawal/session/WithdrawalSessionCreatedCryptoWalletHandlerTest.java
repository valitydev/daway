package dev.vality.daway.handler.event.stock.impl.withdrawal.session;

import dev.vality.daway.config.KafkaPostgresqlSpringBootITest;
import dev.vality.daway.domain.tables.pojos.Destination;
import dev.vality.daway.domain.tables.pojos.WithdrawalSession;
import dev.vality.daway.utils.WithdrawalSessionCreatedHandlerUtils;
import dev.vality.mapper.RecordRowMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Objects;

import static dev.vality.daway.domain.tables.WithdrawalSession.WITHDRAWAL_SESSION;
import static dev.vality.daway.utils.WithdrawalSessionCreatedHandlerUtils.createSession;

@KafkaPostgresqlSpringBootITest
public class WithdrawalSessionCreatedCryptoWalletHandlerTest {

    @Autowired
    private WithdrawalSessionCreatedHandler withdrawalSessionCreatedHandler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    Destination destination = dev.vality.testcontainers.annotations.util.RandomBeans.random(Destination.class);
    String sqlStatement = "select * from dw.withdrawal_session LIMIT 1;";

    @BeforeEach
    public void setUp() {
        destination.setCurrent(true);
    }

    @Test
    public void cryptoWalletTest() {
        dev.vality.fistful.base.Resource resource = new dev.vality.fistful.base.Resource();
        resource.setCryptoWallet(WithdrawalSessionCreatedHandlerUtils.createDestinationResourceCryptoWallet());
        dev.vality.fistful.withdrawal_session.Session session = createSession(resource);

        withdrawalSessionCreatedHandler.handle(
                WithdrawalSessionCreatedHandlerUtils.createCreated(session),
                WithdrawalSessionCreatedHandlerUtils.createCreatedMachineEvent(destination.getDestinationId(), session)
        );

        WithdrawalSession result = jdbcTemplate.queryForObject(sqlStatement,
                new RecordRowMapper<>(WITHDRAWAL_SESSION, WithdrawalSession.class));

        Assertions.assertNotNull(Objects.requireNonNull(result).getResourceCryptoWalletId());
        Assertions.assertNotNull(result.getResourceCryptoWalletType());
    }

}