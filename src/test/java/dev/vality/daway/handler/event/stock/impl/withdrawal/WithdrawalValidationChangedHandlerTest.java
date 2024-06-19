package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.TestData;
import dev.vality.daway.config.PostgresqlJooqSpringBootITest;
import dev.vality.daway.dao.withdrawal.impl.WithdrawalValidationDaoImpl;
import dev.vality.daway.domain.enums.WithdrawalValidationStatus;
import dev.vality.daway.domain.enums.WithdrawalValidationType;
import dev.vality.daway.domain.tables.records.WithdrawalValidationRecord;
import dev.vality.daway.factory.machine.event.WithdrawalValidationMachineEventCopyFactoryImpl;
import dev.vality.fistful.withdrawal.Change;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.fistful.withdrawal.ValidationChange;
import dev.vality.fistful.withdrawal.ValidationResult;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static dev.vality.daway.domain.tables.WithdrawalValidation.WITHDRAWAL_VALIDATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@PostgresqlJooqSpringBootITest
@ContextConfiguration(classes = {WithdrawalValidationDaoImpl.class, WithdrawalValidationChangedHandler.class,
        WithdrawalValidationMachineEventCopyFactoryImpl.class})
class WithdrawalValidationChangedHandlerTest {

    @Autowired
    private WithdrawalValidationChangedHandler handler;

    @Autowired
    DSLContext dslContext;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(WITHDRAWAL_VALIDATION).execute();
    }

    @Test
    void handleReceiver() {
        ValidationResult validationResult = TestData.testValidationResult();
        ValidationChange validationChange = new ValidationChange();
        validationChange.setReceiver(validationResult);
        Change change = new Change();
        change.setValidation(validationChange);
        TimestampedChange timestampedChange = new TimestampedChange();
        timestampedChange.setOccuredAt("2023-07-03T10:15:30Z");
        timestampedChange.setChange(change);

        handler.handle(timestampedChange, TestData.createMachineEvent(timestampedChange));

        WithdrawalValidationRecord record = dslContext.fetchAny(WITHDRAWAL_VALIDATION);
        assertNotNull(record);
        assertEquals(validationResult.getPersonal().getValidationId(), record.getValidationId());
        assertEquals(validationResult.getPersonal().getToken(), record.getPersonalDataToken());
        assertEquals(WithdrawalValidationType.receiver, record.getType());
        assertEquals(WithdrawalValidationStatus.valid, record.getStatus());
    }

    @Test
    void handleSender() {
        ValidationResult validationResult = TestData.testValidationResult();
        ValidationChange validationChange = new ValidationChange();
        validationChange.setSender(validationResult);
        Change change = new Change();
        change.setValidation(validationChange);
        TimestampedChange timestampedChange = new TimestampedChange();
        timestampedChange.setOccuredAt("2023-07-03T10:15:30Z");
        timestampedChange.setChange(change);

        handler.handle(timestampedChange, TestData.createMachineEvent(timestampedChange));

        WithdrawalValidationRecord record = dslContext.fetchAny(WITHDRAWAL_VALIDATION);
        assertNotNull(record);
        assertEquals(validationResult.getPersonal().getValidationId(), record.getValidationId());
        assertEquals(validationResult.getPersonal().getToken(), record.getPersonalDataToken());
        assertEquals(WithdrawalValidationType.sender, record.getType());
        assertEquals(WithdrawalValidationStatus.valid, record.getStatus());
    }
}