package dev.vality.daway.kafka;

import dev.vality.daway.config.KafkaPostgresqlSpringBootITest;
import dev.vality.daway.dao.exrate.iface.ExchangeRateDao;
import dev.vality.daway.domain.tables.pojos.ExRate;
import dev.vality.daway.service.ExchangeRateService;
import dev.vality.exrates.base.Currency;
import dev.vality.exrates.base.Rational;
import dev.vality.exrates.events.CurrencyEvent;
import dev.vality.exrates.events.CurrencyEventPayload;
import dev.vality.exrates.events.CurrencyExchangeRate;
import dev.vality.geck.common.util.TypeUtil;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@KafkaPostgresqlSpringBootITest
public class ExchangeRateKafkaListenerTest {

    @Value("${kafka.topics.exrate.id}")
    public String topic;

    @Autowired
    private dev.vality.testcontainers.annotations.kafka.config.KafkaProducer<TBase<?, ?>> testThriftKafkaProducer;

    @MockitoSpyBean
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeRateDao exchangeRateDao;

    @Test
    public void listenExchangeRateEventTest() {
        // Given
        CurrencyEvent currencyEvent = buildCurrencyEvent();
        CurrencyExchangeRate exchangeRate = currencyEvent.payload.getExchangeRate();

        // When
        testThriftKafkaProducer.send(topic, currencyEvent);
        await().atMost(30, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() -> {
            return exchangeRateDao.findBySourceSymbolicCode(exchangeRate.getSourceCurrency().getSymbolicCode()) != null;
        });
        ExRate exrate = exchangeRateDao.findBySourceSymbolicCode(exchangeRate.getSourceCurrency().getSymbolicCode());

        // Then
        verify(exchangeRateService, timeout(TimeUnit.MINUTES.toMillis(1)).times(1)).handleEvents(anyList());
        assertEquals(exchangeRate.getSourceCurrency().getSymbolicCode(), exrate.getSourceCurrencySymbolicCode());
        assertEquals(exchangeRate.getSourceCurrency().getExponent(), exrate.getSourceCurrencyExponent());
        assertEquals(exchangeRate.getDestinationCurrency().getSymbolicCode(), exrate.getDestinationCurrencySymbolicCode());
        assertEquals(exchangeRate.getDestinationCurrency().getExponent(), exrate.getDestinationCurrencyExponent());
        assertEquals(exchangeRate.getExchangeRate().p, exrate.getRationalP());
        assertEquals(exchangeRate.getExchangeRate().q, exrate.getRationalQ());
        assertEquals(TypeUtil.stringToLocalDateTime(exchangeRate.getTimestamp()).truncatedTo(ChronoUnit.SECONDS),
                exrate.getRateTimestamp().truncatedTo(ChronoUnit.SECONDS));
    }

    private CurrencyEvent buildCurrencyEvent() {
        CurrencyEvent currencyEvent = new CurrencyEvent();
        currencyEvent.setEventId(UUID.randomUUID().toString());
        currencyEvent.setEventCreatedAt(TypeUtil.temporalToString(LocalDateTime.now()));
        currencyEvent.setPayload(
                CurrencyEventPayload.exchange_rate(
                        new CurrencyExchangeRate(
                                new Currency("USD", (short) 2),
                                new Currency("RUB", (short) 2),
                                new Rational(60797502, 1000000),
                                TypeUtil.temporalToString(Instant.now())
                        )
                )
        );

        return currencyEvent;
    }

}
