package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.TestData;
import dev.vality.daway.config.PostgresqlJooqSpringBootITest;
import dev.vality.daway.dao.withdrawal.impl.FistfulCashFlowDaoImpl;
import dev.vality.daway.dao.withdrawal.impl.WithdrawalAdjustmentDaoImpl;
import dev.vality.daway.domain.enums.WithdrawalTransferStatus;
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

import static dev.vality.daway.domain.tables.FistfulCashFlow.FISTFUL_CASH_FLOW;
import static dev.vality.daway.domain.tables.WithdrawalAdjustment.WITHDRAWAL_ADJUSTMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@PostgresqlJooqSpringBootITest
@ContextConfiguration(classes = {WithdrawalAdjustmentDaoImpl.class, FistfulCashFlowDaoImpl.class,
        WithdrawalAdjustmentTransferCreatedHandler.class,
        WithdrawalAdjustmentMachineEventCopyFactoryImpl.class,})
class WithdrawalAdjustmentTransferCreatedHandlerTest {

    @Autowired
    WithdrawalAdjustmentTransferCreatedHandler handler;

    @Autowired
    DSLContext dslContext;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(WITHDRAWAL_ADJUSTMENT).execute();
        dslContext.deleteFrom(FISTFUL_CASH_FLOW).execute();
    }

    @Test
    void handle() {
        String adjustmentId = "adjustment_id";
        WithdrawalAdjustment withdrawalAdjustment = TestData.createWithdrawalAdjustment(adjustmentId);
        dslContext.insertInto(WITHDRAWAL_ADJUSTMENT)
                .set(dslContext.newRecord(WITHDRAWAL_ADJUSTMENT, withdrawalAdjustment))
                .execute();
        TimestampedChange timestampedChange = TestData.createWithdrawalAdjustmentTransferCreatedChange(adjustmentId);

        MachineEvent event = TestData.createMachineEvent(timestampedChange);
        event.setSourceId(withdrawalAdjustment.getWithdrawalId());

        handler.handle(timestampedChange, event);

        Result<WithdrawalAdjustmentRecord> recordNew = dslContext.fetch(WITHDRAWAL_ADJUSTMENT, WITHDRAWAL_ADJUSTMENT.CURRENT.eq(Boolean.TRUE));
        assertEquals(1, recordNew.size());
        assertEquals(WithdrawalTransferStatus.created, recordNew.get(0).getWithdrawalTransferStatus());
        assertNotNull(recordNew.get(0).getFee());
        assertNotNull(recordNew.get(0).getProviderFee());
        assertEquals(4, dslContext.fetchCount(FISTFUL_CASH_FLOW));
    }
}