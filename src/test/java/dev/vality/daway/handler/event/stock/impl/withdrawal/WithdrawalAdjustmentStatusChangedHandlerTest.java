package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.TestData;
import dev.vality.daway.config.PostgresqlJooqSpringBootITest;
import dev.vality.daway.dao.withdrawal.impl.WithdrawalAdjustmentDaoImpl;
import dev.vality.daway.domain.enums.WithdrawalAdjustmentStatus;
import dev.vality.daway.domain.tables.pojos.WithdrawalAdjustment;
import dev.vality.daway.domain.tables.records.WithdrawalAdjustmentRecord;
import dev.vality.daway.factory.machine.event.WithdrawalAdjustmentMachineEventCopyFactoryImpl;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static dev.vality.daway.domain.tables.WithdrawalAdjustment.WITHDRAWAL_ADJUSTMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlJooqSpringBootITest
@ContextConfiguration(classes = {WithdrawalAdjustmentDaoImpl.class, WithdrawalAdjustmentStatusChangedHandler.class,
        WithdrawalAdjustmentMachineEventCopyFactoryImpl.class,})
class WithdrawalAdjustmentStatusChangedHandlerTest {

    @Autowired
    WithdrawalAdjustmentStatusChangedHandler handler;

    @Autowired
    DSLContext dslContext;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(WITHDRAWAL_ADJUSTMENT).execute();
    }

    @Test
    void handle() {
        String adjustmentId = "adjustment_id";
        WithdrawalAdjustment withdrawalAdjustment = TestData.createWithdrawalAdjustment(adjustmentId);
        dslContext.insertInto(WITHDRAWAL_ADJUSTMENT)
                .set(dslContext.newRecord(WITHDRAWAL_ADJUSTMENT, withdrawalAdjustment))
                .execute();
        TimestampedChange timestampedChange = TestData.createWithdrawalAdjustmentStatusChange(adjustmentId);
        MachineEvent event = TestData.createWithdrawalAdjustmentdMachineEvent(timestampedChange);
        event.setSourceId(withdrawalAdjustment.getWithdrawalId());

        handler.handle(timestampedChange, event);

        Result<WithdrawalAdjustmentRecord> recordNew = dslContext.fetch(WITHDRAWAL_ADJUSTMENT, WITHDRAWAL_ADJUSTMENT.CURRENT.eq(Boolean.TRUE));
        assertEquals(1, recordNew.size());
        assertEquals(WithdrawalAdjustmentStatus.succeeded, recordNew.get(0).getStatus());
        Result<WithdrawalAdjustmentRecord> recordOld = dslContext.fetch(WITHDRAWAL_ADJUSTMENT, WITHDRAWAL_ADJUSTMENT.CURRENT.eq(Boolean.FALSE));
        assertEquals(1, recordOld.size());
        assertEquals(WithdrawalAdjustmentStatus.pending, recordOld.get(0).getStatus());
    }
}
