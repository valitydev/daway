package dev.vality.daway.factory.cash.flow;

import dev.vality.damsel.domain.FinalCashFlowPosting;
import dev.vality.daway.TestData;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CashFlowFactoryTest {

    @Test
    void buildWithExchangeContextTest() {
        FinalCashFlowPosting posting = TestData.createPaymentCashFlowPostingWithExchangeContext();

        CashFlow cashFlow = CashFlowFactory.build(List.of(posting), 1L, PaymentChangeType.payment).get(0);

        assertEquals("RUB", cashFlow.getExchangeSourceCurrencyCode());
        assertEquals("USD", cashFlow.getExchangeDestinationCurrencyCode());
        assertEquals(60797502L, cashFlow.getExchangeRateRationalP());
        assertEquals(1000000L, cashFlow.getExchangeRateRationalQ());
    }

    @Test
    void buildWithoutExchangeContextTest() {
        CashFlow cashFlow = CashFlowFactory.build(
                List.of(TestData.createPaymentCashFlowPosting()),
                1L,
                PaymentChangeType.payment
        ).get(0);

        assertNull(cashFlow.getExchangeSourceCurrencyCode());
        assertNull(cashFlow.getExchangeDestinationCurrencyCode());
        assertNull(cashFlow.getExchangeRateRationalP());
        assertNull(cashFlow.getExchangeRateRationalQ());
    }
}
