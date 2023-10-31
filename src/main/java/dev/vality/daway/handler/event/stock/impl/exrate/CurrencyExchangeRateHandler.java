package dev.vality.daway.handler.event.stock.impl.exrate;

import dev.vality.daway.dao.exrate.iface.ExchangeRateDao;
import dev.vality.daway.domain.tables.pojos.ExRate;
import dev.vality.exrates.events.CurrencyEvent;
import dev.vality.exrates.events.CurrencyExchangeRate;
import dev.vality.geck.common.util.TypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyExchangeRateHandler implements ExchangeRateHandler {

    private final ExchangeRateDao exchangeRateDao;

    @Override
    public void handle(List<CurrencyEvent> events) {
        List<ExRate> exrates = events.stream().map(currencyEvent -> {
            CurrencyExchangeRate exchangeRate = currencyEvent.getPayload().getExchangeRate();
            ExRate exrate = new ExRate();
            exrate.setEventId(UUID.fromString(currencyEvent.getEventId()));
            exrate.setEventCreatedAt(TypeUtil.stringToLocalDateTime(currencyEvent.getEventCreatedAt()));
            exrate.setSourceCurrencySymbolicCode(exchangeRate.getSourceCurrency().getSymbolicCode());
            exrate.setSourceCurrencyExponent(exchangeRate.getSourceCurrency().getExponent());
            exrate.setDestinationCurrencySymbolicCode(exchangeRate.getDestinationCurrency().getSymbolicCode());
            exrate.setDestinationCurrencyExponent(exchangeRate.getDestinationCurrency().getExponent());
            exrate.setRationalP(exchangeRate.getExchangeRate().getP());
            exrate.setRationalQ(exchangeRate.getExchangeRate().getQ());
            exrate.setRateTimestamp(TypeUtil.stringToLocalDateTime(exchangeRate.timestamp));
            return exrate;
        }).collect(Collectors.toList());

        exchangeRateDao.saveBatch(exrates);
    }

    @Override
    public boolean isHandle(CurrencyEvent currencyEvent) {
        return currencyEvent.payload.isSetExchangeRate();
    }
}
