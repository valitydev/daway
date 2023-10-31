package dev.vality.daway.factory.machine.event;

import dev.vality.daway.domain.tables.pojos.WithdrawalSession;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalSessionMachineEventCopyFactoryImpl
        implements MachineEventCopyFactory<WithdrawalSession, String> {

    @Override
    public WithdrawalSession create(MachineEvent event,
                                    Long sequenceId,
                                    String withdrawalSessionId,
                                    WithdrawalSession withdrawalSessionOld,
                                    String occurredAt) {
        WithdrawalSession withdrawalSession = null;
        if (withdrawalSessionOld != null) {
            withdrawalSession = new WithdrawalSession(withdrawalSessionOld);
        } else {
            withdrawalSession = new WithdrawalSession();
        }
        withdrawalSession.setId(null);
        withdrawalSession.setWtime(null);
        withdrawalSession.setSequenceId(sequenceId.intValue());
        withdrawalSession.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        withdrawalSession.setEventOccuredAt(TypeUtil.stringToLocalDateTime(occurredAt));
        withdrawalSession.setWithdrawalSessionId(withdrawalSessionId);
        return withdrawalSession;
    }

    @Override
    public WithdrawalSession create(MachineEvent event, Long sequenceId, String id, String occurredAt) {
        return create(event, sequenceId, id, null, occurredAt);
    }

}
