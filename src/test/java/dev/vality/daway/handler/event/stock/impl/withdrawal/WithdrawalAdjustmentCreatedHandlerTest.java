package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.TestData;
import dev.vality.daway.config.PostgresqlJooqSpringBootITest;
import dev.vality.daway.dao.withdrawal.impl.WithdrawalAdjustmentDaoImpl;
import dev.vality.daway.domain.tables.records.WithdrawalAdjustmentRecord;
import dev.vality.daway.factory.machine.event.WithdrawalAdjustmentMachineEventCopyFactoryImpl;
import dev.vality.fistful.withdrawal.TimestampedChange;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static dev.vality.daway.domain.tables.WithdrawalAdjustment.WITHDRAWAL_ADJUSTMENT;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@PostgresqlJooqSpringBootITest
@ContextConfiguration(classes = {WithdrawalAdjustmentDaoImpl.class, WithdrawalAdjustmentCreatedHandler.class,
        WithdrawalAdjustmentMachineEventCopyFactoryImpl.class,})
class WithdrawalAdjustmentCreatedHandlerTest {

    @Autowired
    WithdrawalAdjustmentCreatedHandler handler;

    @Autowired
    DSLContext dslContext;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(WITHDRAWAL_ADJUSTMENT).execute();
    }

    @Test
    void handledStatusChange() {
        TimestampedChange timestampedChange = TestData.createWithdrawalAdjustmentCreatedChange("adjustmentId");

        handler.handle(timestampedChange, TestData.createWithdrawalAdjustmentdMachineEvent(timestampedChange));

        WithdrawalAdjustmentRecord record = dslContext.fetchAny(WITHDRAWAL_ADJUSTMENT);
        assertNotNull(record);
        assertNotNull(record.getWithdrawalStatus());
        assertNull(record.getDomainRevision());
    }

    @Test
    void handleDomainRevisionChange() {
        TimestampedChange timestampedChange = TestData.createWithdrawalAdjustmentCreatedDomainRevisionChange("adjustmentId");

        handler.handle(timestampedChange, TestData.createWithdrawalAdjustmentdMachineEvent(timestampedChange));

        WithdrawalAdjustmentRecord record = dslContext.fetchAny(WITHDRAWAL_ADJUSTMENT);
        assertNotNull(record);
        assertNull(record.getWithdrawalStatus());
        assertNotNull(record.getDomainRevision());
    }
}
