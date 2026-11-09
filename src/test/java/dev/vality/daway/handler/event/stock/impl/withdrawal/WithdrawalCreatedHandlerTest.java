package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.TestData;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalDao;
import dev.vality.daway.domain.tables.pojos.Withdrawal;
import dev.vality.daway.factory.machine.event.WithdrawalMachineEventCopyFactoryImpl;
import dev.vality.daway.service.ExchangeRateCalculationService;
import dev.vality.fistful.base.ClientIdentity;
import dev.vality.fistful.base.ContactInfo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WithdrawalCreatedHandlerTest {

    @Test
    void savesIdentitySeparatelyFromEmail() {
        assertIdentity(new ClientIdentity("customer").setUserId("user"), "customer", "user");
    }

    @Test
    void supportsIdentityWithoutUserId() {
        assertIdentity(new ClientIdentity("customer"), "customer", null);
    }

    @Test
    void doesNotUseEmailAsCustomerIdForLegacyEvents() {
        assertIdentity(null, null, null);
    }

    private void assertIdentity(ClientIdentity identity, String customerId, String userId) {
        var dao = mock(WithdrawalDao.class);
        var handler = new WithdrawalCreatedHandler(dao, new WithdrawalMachineEventCopyFactoryImpl(),
                mock(ExchangeRateCalculationService.class));
        var change = TestData.createWithdrawalCreatedChange("withdrawal");
        change.getChange().getCreated().getWithdrawal()
                .setClientIdentity(identity)
                .setContactInfo(new ContactInfo().setEmail("contact@example.com"));
        handler.handle(change, TestData.createInvoiceEvent("withdrawal", 1L, LocalDateTime.now()));
        var captor = ArgumentCaptor.forClass(Withdrawal.class);
        verify(dao).save(captor.capture());
        assertEquals(customerId, captor.getValue().getCustomerId());
        assertEquals(userId, captor.getValue().getUserId());
        assertEquals("contact@example.com", captor.getValue().getEmail());
    }
}
