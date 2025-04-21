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
import org.springframework.test.context.jdbc.Sql;

import java.util.Objects;

import static dev.vality.daway.domain.tables.WithdrawalSession.WITHDRAWAL_SESSION;
import static dev.vality.daway.utils.WithdrawalSessionCreatedHandlerUtils.createSession;

@KafkaPostgresqlSpringBootITest
@Sql(scripts = "classpath:sql/partition/withdrawal_session.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WithdrawalSessionCreatedDigitalWalletHandlerTest {

    @Autowired
    private WithdrawalSessionCreatedHandler withdrawalSessionCreatedHandler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    Destination destination = dev.vality.testcontainers.annotations.util.RandomBeans.random(Destination.class);
    String sqlStatement = "select * from dw.withdrawal_session LIMIT 1;";

    @BeforeEach
    void setUp() {
        destination.setCurrent(true);
    }

    @Test
    void digitalWalletTest() {
        dev.vality.fistful.base.Resource resource = new dev.vality.fistful.base.Resource();
        resource.setDigitalWallet(WithdrawalSessionCreatedHandlerUtils.createDestinationResourceDigitalWallet());
        dev.vality.fistful.withdrawal_session.Session session = createSession(resource);

        withdrawalSessionCreatedHandler.handle(
                WithdrawalSessionCreatedHandlerUtils.createCreated(session),
                WithdrawalSessionCreatedHandlerUtils.createCreatedMachineEvent(destination.getDestinationId(), session)
        );

        WithdrawalSession result = jdbcTemplate.queryForObject(sqlStatement,
                new RecordRowMapper<>(WITHDRAWAL_SESSION, WithdrawalSession.class));

        Assertions.assertNotNull(Objects.requireNonNull(result).getResourceDigitalWalletId());
        Assertions.assertNotNull(result.getResourceDigitalWalletData());
    }
}
