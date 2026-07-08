package dev.vality.daway.factory.cash.flow;

import dev.vality.damsel.base.Rational;
import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.CashFlowAccount;
import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.domain.ExchangeContext;
import dev.vality.damsel.domain.FinalCashFlowAccount;
import dev.vality.damsel.domain.FinalCashFlowPosting;
import dev.vality.damsel.domain.MerchantCashFlowAccount;
import dev.vality.damsel.domain.SystemCashFlowAccount;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CashFlowFactoryTest {

    @Test
    void buildWithExchangeContextTest() {
        FinalCashFlowPosting posting = buildPosting();
        posting.setExchangeContext(new ExchangeContext("RUB", "USD", new Rational(60797502L, 1000000L)));

        CashFlow cashFlow = CashFlowFactory.build(List.of(posting), 1L, PaymentChangeType.payment).get(0);

        assertEquals("RUB", cashFlow.getExchangeSourceCurrencyCode());
        assertEquals("USD", cashFlow.getExchangeDestinationCurrencyCode());
        assertEquals(60797502L, cashFlow.getExchangeRateRationalP());
        assertEquals(1000000L, cashFlow.getExchangeRateRationalQ());
    }

    @Test
    void buildWithoutExchangeContextTest() {
        CashFlow cashFlow = CashFlowFactory.build(List.of(buildPosting()), 1L, PaymentChangeType.payment).get(0);

        assertNull(cashFlow.getExchangeSourceCurrencyCode());
        assertNull(cashFlow.getExchangeDestinationCurrencyCode());
        assertNull(cashFlow.getExchangeRateRationalP());
        assertNull(cashFlow.getExchangeRateRationalQ());
    }

    private FinalCashFlowPosting buildPosting() {
        return new FinalCashFlowPosting()
                .setSource(new FinalCashFlowAccount()
                        .setAccountId(1)
                        .setAccountType(CashFlowAccount.merchant(MerchantCashFlowAccount.settlement)))
                .setDestination(new FinalCashFlowAccount()
                        .setAccountId(2)
                        .setAccountType(CashFlowAccount.system(SystemCashFlowAccount.settlement)))
                .setVolume(new Cash(1000L, new CurrencyRef("RUB")));
    }
}
