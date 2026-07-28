package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.TestData;
import dev.vality.daway.config.PostgresqlJooqSpringBootITest;
import dev.vality.daway.dao.withdrawal.impl.WithdrawalCashChangeDaoImpl;
import dev.vality.daway.domain.tables.records.WithdrawalCashChangeRecord;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static dev.vality.daway.domain.tables.WithdrawalCashChange.WITHDRAWAL_CASH_CHANGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PostgresqlJooqSpringBootITest
@ContextConfiguration(classes = {WithdrawalCashChangeDaoImpl.class, WithdrawalBodyChangedHandler.class})
class WithdrawalBodyChangedHandlerTest {

    @Autowired
    private WithdrawalBodyChangedHandler handler;

    @Autowired
    private DSLContext dslContext;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(WITHDRAWAL_CASH_CHANGE).execute();
    }

    @Test
    void handle() {
        TimestampedChange timestampedChange = TestData.createWithdrawalBodyChangedChange();
        MachineEvent event = TestData.createMachineEvent(timestampedChange);

        handler.handle(timestampedChange, event);

        WithdrawalCashChangeRecord record = dslContext.fetchAny(WITHDRAWAL_CASH_CHANGE);
        assertNotNull(record);
        assertEquals(event.getSourceId(), record.getWithdrawalId());
        assertEquals(event.getEventId(), record.getSequenceId());
        assertEquals(100L, record.getOldAmount());
        assertEquals("RUB", record.getOldCurrencyCode());
        assertEquals(200L, record.getNewAmount());
        assertEquals("USD", record.getNewCurrencyCode());
        assertTrue(record.getCurrent());
    }
}
