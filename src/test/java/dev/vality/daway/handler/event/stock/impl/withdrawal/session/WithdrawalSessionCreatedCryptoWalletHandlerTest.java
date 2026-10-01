package dev.vality.daway.handler.event.stock.impl.withdrawal.session;

import dev.vality.daway.integration.base.AbstractKafkaIntegrationTest;
import dev.vality.daway.config.KafkaPostgresqlSpringBootITest;
import dev.vality.daway.domain.tables.pojos.WithdrawalSession;
import dev.vality.daway.utils.WithdrawalSessionCreatedHandlerUtils;
import dev.vality.mapper.RecordRowMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.Objects;
import java.util.UUID;

import static dev.vality.daway.domain.tables.WithdrawalSession.WITHDRAWAL_SESSION;
import static dev.vality.daway.utils.WithdrawalSessionCreatedHandlerUtils.createSession;

@KafkaPostgresqlSpringBootITest
@Sql(scripts = {"classpath:sql/partition_idx.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class WithdrawalSessionCreatedCryptoWalletHandlerTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private WithdrawalSessionCreatedHandler withdrawalSessionCreatedHandler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    String sqlStatement = "select * from dw.withdrawal_session where withdrawal_session_id = ?;";

    @Test
    void cryptoWalletTest() {
        String sessionId = UUID.randomUUID().toString();
        dev.vality.fistful.base.Resource resource = new dev.vality.fistful.base.Resource();
        resource.setCryptoWallet(WithdrawalSessionCreatedHandlerUtils.createDestinationResourceCryptoWallet());
        dev.vality.fistful.withdrawal_session.Session session = createSession(resource);
        session.setId(sessionId);

        withdrawalSessionCreatedHandler.handle(
                WithdrawalSessionCreatedHandlerUtils.createCreated(session),
                WithdrawalSessionCreatedHandlerUtils.createCreatedMachineEvent(sessionId, session)
        );

        WithdrawalSession result = jdbcTemplate.queryForObject(sqlStatement,
                new RecordRowMapper<>(WITHDRAWAL_SESSION, WithdrawalSession.class), sessionId);

        Assertions.assertNotNull(Objects.requireNonNull(result).getResourceCryptoWalletId());
        Assertions.assertNotNull(result.getResourceCryptoWalletType());
    }
}
