package dev.vality.daway.service;

import dev.vality.daway.handler.event.stock.impl.exrate.ExchangeRateHandler;
import dev.vality.exrates.events.CurrencyEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final List<ExchangeRateHandler> exchangeRateHandlers;

    @Transactional(propagation = Propagation.REQUIRED)
    public void handleEvents(List<CurrencyEvent> events) {
        events.stream()
                .collect(Collectors.groupingBy(
                        currencyEvent -> exchangeRateHandlers.stream()
                                .filter(exchangeRateHandler -> exchangeRateHandler.isHandle(currencyEvent))
                                .findAny().orElseThrow())
                ).forEach(ExchangeRateHandler::handle);
    }

}
