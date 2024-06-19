package dev.vality.daway.factory.machine.event;

import dev.vality.daway.domain.tables.pojos.WithdrawalValidation;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalValidationMachineEventCopyFactoryImpl implements MachineEventCopyFactory<WithdrawalValidation, String> {

    @Override
    public WithdrawalValidation create(MachineEvent event, Long sequenceId, String id, WithdrawalValidation old, String occurredAt) {
        WithdrawalValidation withdrawalValidation = null;
        if (old != null) {
            withdrawalValidation = new WithdrawalValidation(old);
        } else {
            withdrawalValidation = new WithdrawalValidation();
        }
        withdrawalValidation.setId(null);
        withdrawalValidation.setWtime(null);
        withdrawalValidation.setValidationId(id);
        withdrawalValidation.setSequenceId(sequenceId);
        withdrawalValidation.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        withdrawalValidation.setEventOccuredAt(TypeUtil.stringToLocalDateTime(occurredAt));
        return withdrawalValidation;
    }

    @Override
    public WithdrawalValidation create(MachineEvent event, Long sequenceId, String id, String occurredAt) {
        return create(event, sequenceId, id, null, occurredAt);
    }

}
