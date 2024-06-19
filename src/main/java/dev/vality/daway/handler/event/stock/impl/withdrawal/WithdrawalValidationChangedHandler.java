package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.dao.withdrawal.iface.WithdrawalValidationDao;
import dev.vality.daway.domain.enums.WithdrawalValidationStatus;
import dev.vality.daway.domain.enums.WithdrawalValidationType;
import dev.vality.daway.domain.tables.pojos.WithdrawalValidation;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.fistful.withdrawal.*;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalValidationChangedHandler implements WithdrawalHandler {

    private final WithdrawalValidationDao withdrawalValidationDao;
    private final MachineEventCopyFactory<WithdrawalValidation, String> machineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.validation", new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        long sequenceId = event.getEventId();
        String withdrawalId = event.getSourceId();
        Change change = timestampedChange.getChange();
        ValidationChange validationChange = change.getValidation();
        var validationResult = getValidationResult(validationChange);
        var personalDataValidationResult = validationResult.getPersonal();
        String validationId = personalDataValidationResult.getValidationId();
        log.info("Start withdrawal validation changed handling, sequenceId={}, withdrawalId={}, validationId={}",
                sequenceId, withdrawalId, validationId);

        WithdrawalValidation withdrawalValidation = machineEventCopyFactory
                .create(event, sequenceId, validationId, timestampedChange.getOccuredAt());

        withdrawalValidation.setWithdrawalId(withdrawalId);
        withdrawalValidation.setType(
                validationChange.isSetReceiver()
                        ? WithdrawalValidationType.receiver
                        : WithdrawalValidationType.sender
        );
        withdrawalValidation.setPersonalDataToken(personalDataValidationResult.getToken());
        withdrawalValidation.setStatus(
                personalDataValidationResult.getValidationStatus().equals(ValidationStatus.valid)
                        ? WithdrawalValidationStatus.valid
                        : WithdrawalValidationStatus.invalid
        );


        withdrawalValidationDao.save(withdrawalValidation).ifPresentOrElse(
                dbId -> log.info("Withdrawal validation have been changed, sequenceId={}, withdrawalId={}, validationId={}",
                        sequenceId, withdrawalId, validationId),
                () -> log.info("Withdrawal validation duplicated, sequenceId={}, withdrawalId={}, validationId={}",
                        sequenceId, withdrawalId, validationId)
        );
    }

    private ValidationResult getValidationResult(ValidationChange change) {
        if (change.isSetReceiver()) {
            return change.getReceiver();
        } else {
            return change.getSender();
        }
    }

}
