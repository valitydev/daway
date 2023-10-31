package dev.vality.daway.service;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.payment_processing.*;
import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.domain.enums.PaymentToolType;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.msgpack.Value;
import dev.vality.sink.common.serialization.impl.ThriftBinarySerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@PostgresqlSpringBootITest
public class RecurrentPaymentToolServiceTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private RecurrentPaymentToolService recurrentPaymentToolService;

    @Test
    public void handleEventsTest() {
        String recurrentId = "recurrentId";
        List<MachineEvent> events = buildEvent(recurrentId);
        recurrentPaymentToolService.handleEvents(events);

        String sql = """
                select * 
                from dw.recurrent_payment_tool
                where recurrent_payment_tool_id = :id
                order by sequence_id, change_id asc
                """;
        List<dev.vality.daway.domain.tables.pojos.RecurrentPaymentTool> recurrentPaymentTools =
                jdbcTemplate.query(sql, new MapSqlParameterSource("id", recurrentId),
                        new BeanPropertyRowMapper<>(
                                dev.vality.daway.domain.tables.pojos.RecurrentPaymentTool.class));
        Assertions.assertEquals(7, recurrentPaymentTools.size());

        var created = recurrentPaymentTools.get(0);
        Assertions.assertEquals(recurrentId, created.getRecurrentPaymentToolId());
        Assertions.assertEquals("shop_id", created.getShopId());
        Assertions.assertEquals(dev.vality.daway.domain.enums.RecurrentPaymentToolStatus.created, created.getStatus());
        Assertions.assertEquals(PaymentToolType.bank_card, created.getPaymentToolType());
        Assertions.assertEquals(123, created.getAmount().longValue());
        Assertions.assertEquals("high", created.getRiskScore());
        Assertions.assertEquals(54, created.getRouteProviderId().intValue());
        Assertions.assertFalse(created.getCurrent());

        var riskScoreChanged = recurrentPaymentTools.get(1);
        Assertions.assertEquals("fatal", riskScoreChanged.getRiskScore());
        Assertions.assertNotEquals(created.getId(), riskScoreChanged.getId());
        Assertions.assertFalse(riskScoreChanged.getCurrent());

        var routeChanged = recurrentPaymentTools.get(2);
        Assertions.assertEquals(123, routeChanged.getRouteProviderId().longValue());
        Assertions.assertEquals(456, routeChanged.getRouteTerminalId().longValue());
        Assertions.assertFalse(routeChanged.getCurrent());

        var abandoned = recurrentPaymentTools.get(3);
        Assertions.assertEquals(dev.vality.daway.domain.enums.RecurrentPaymentToolStatus.abandoned, abandoned.getStatus());
        Assertions.assertFalse(abandoned.getCurrent());

        var acquired = recurrentPaymentTools.get(4);
        Assertions.assertEquals("kek_token", acquired.getRecToken());
        Assertions.assertNotEquals(created.getRecToken(), acquired.getRecToken());
        Assertions.assertFalse(acquired.getCurrent());

        var failed = recurrentPaymentTools.get(5);
        Assertions.assertEquals(dev.vality.daway.domain.enums.RecurrentPaymentToolStatus.failed, failed.getStatus());
        Assertions.assertFalse(failed.getCurrent());

        var sessionChanged = recurrentPaymentTools.get(6);
        Assertions.assertEquals("trxId", sessionChanged.getSessionPayloadTransactionBoundTrxId());
        Assertions.assertEquals("rrn", sessionChanged.getSessionPayloadTransactionBoundTrxAdditionalInfoRrn());
        Assertions.assertTrue(sessionChanged.getCurrent());

    }

    private List<MachineEvent> buildEvent(String recurrentId) {
        ThriftBinarySerializer<RecurrentPaymentToolEventData> serializer = new ThriftBinarySerializer<>();
        return Collections.singletonList(new MachineEvent()
                .setSourceId("")
                .setEventId(123L)
                .setCreatedAt("2016-03-22T06:12:27Z")
                .setSourceId(recurrentId)
                .setData(Value.bin(serializer.serialize(new RecurrentPaymentToolEventData().setChanges(
                        List.of(
                                RecurrentPaymentToolChange.rec_payment_tool_created(
                                        new RecurrentPaymentToolHasCreated()
                                                .setRecPaymentTool(new RecurrentPaymentTool()
                                                        .setId(recurrentId)
                                                        .setShopId("shop_id")
                                                        .setPartyId("party_id")
                                                        .setPartyRevision(124)
                                                        .setDomainRevision(1245)
                                                        .setStatus(RecurrentPaymentToolStatus
                                                                .created(new RecurrentPaymentToolCreated()))
                                                        .setCreatedAt("2016-03-22T06:12:27Z")
                                                        .setPaymentResource(new DisposablePaymentResource()
                                                                .setPaymentTool(PaymentTool.bank_card(new BankCard()
                                                                        .setToken("kkekekek_token")
                                                                        .setPaymentSystem(new PaymentSystemRef("amex"))
                                                                        .setBin("bin")
                                                                        .setLastDigits("masked")
                                                                        .setPaymentToken(new BankCardTokenServiceRef("applepay"))
                                                                        .setIssuerCountry(CountryCode.ABH)
                                                                        .setBankName("bank_name")
                                                                        .setMetadata(Map.of("kek",
                                                                                dev.vality.damsel.msgpack.Value
                                                                                        .b(true)))))
                                                                .setPaymentSessionId("kek_session_id")
                                                                .setClientInfo(new ClientInfo()
                                                                        .setIpAddress("127.0.0.1")
                                                                        .setFingerprint("kekksiki")))
                                                        .setRecToken("kek_token_111")
                                                        .setRoute(new PaymentRoute()
                                                                .setProvider(new ProviderRef(888))
                                                                .setTerminal(new TerminalRef(9999)))
                                                        .setMinimalPaymentCost(new Cash(123, new CurrencyRef("RUB")))
                                                )
                                                .setRiskScore(RiskScore.high)
                                                .setRoute(new PaymentRoute()
                                                        .setProvider(new ProviderRef(54))
                                                        .setTerminal(new TerminalRef(9883)))
                                ),
                                RecurrentPaymentToolChange.rec_payment_tool_risk_score_changed(
                                        new RecurrentPaymentToolRiskScoreChanged()
                                                .setRiskScore(RiskScore.fatal)
                                ),
                                RecurrentPaymentToolChange.rec_payment_tool_route_changed(
                                        new RecurrentPaymentToolRouteChanged()
                                                .setRoute(new PaymentRoute()
                                                        .setProvider(new ProviderRef(123))
                                                        .setTerminal(new TerminalRef(456)))
                                ),
                                RecurrentPaymentToolChange.rec_payment_tool_abandoned(
                                        new RecurrentPaymentToolHasAbandoned()
                                ),
                                RecurrentPaymentToolChange.rec_payment_tool_acquired(
                                        new RecurrentPaymentToolHasAcquired()
                                                .setToken("kek_token")
                                ),
                                RecurrentPaymentToolChange.rec_payment_tool_failed(
                                        new RecurrentPaymentToolHasFailed()
                                                .setFailure(OperationFailure.failure(new Failure().setCode("code")))
                                ),
                                RecurrentPaymentToolChange.rec_payment_tool_session_changed(
                                        new RecurrentPaymentToolSessionChange()
                                                .setPayload(SessionChangePayload.session_transaction_bound(
                                                        new SessionTransactionBound()
                                                                .setTrx(new TransactionInfo()
                                                                        .setId("trxId")
                                                                        .setExtra(Map.of("lol", "kek"))
                                                                        .setAdditionalInfo(
                                                                                new AdditionalTransactionInfo()
                                                                                        .setRrn("rrn")))

                                                ))
                                )
                        ))))));
    }
}
