package dev.vality.daway.factory.machine.event;

import dev.vality.daway.domain.tables.pojos.WithdrawalAdjustment;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalAdjustmentMachineEventCopyFactoryImpl implements MachineEventCopyFactory<WithdrawalAdjustment, String> {

    @Override
    public WithdrawalAdjustment create(MachineEvent event, Long sequenceId, String id, WithdrawalAdjustment old, String occurredAt) {
        WithdrawalAdjustment withdrawalAdjustment = null;
        if (old != null) {
            withdrawalAdjustment = new WithdrawalAdjustment(old);
        } else {
            withdrawalAdjustment = new WithdrawalAdjustment();
        }
        withdrawalAdjustment.setId(null);
        withdrawalAdjustment.setWtime(null);
        withdrawalAdjustment.setAdjustmentId(id);
        withdrawalAdjustment.setSequenceId(sequenceId);
        withdrawalAdjustment.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        withdrawalAdjustment.setEventOccuredAt(TypeUtil.stringToLocalDateTime(occurredAt));
        return withdrawalAdjustment;
    }

    @Override
    public WithdrawalAdjustment create(MachineEvent event, Long sequenceId, String id, String occurredAt) {
        return create(event, sequenceId, id, null, occurredAt);
    }

}
