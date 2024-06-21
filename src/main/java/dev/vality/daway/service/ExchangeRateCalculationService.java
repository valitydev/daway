package dev.vality.daway.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ExchangeRateCalculationService {

    public BigDecimal calculate(long amountFrom, long amountTo) {
        if (amountFrom == 0L || amountTo == 0L) {
            return null;
        }
        return BigDecimal.valueOf(amountTo).divide(BigDecimal.valueOf(amountFrom), 4, RoundingMode.HALF_UP);
    }
}
