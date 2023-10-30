package dev.vality.daway.factory.machine.event;

import dev.vality.daway.domain.tables.pojos.RecurrentPaymentTool;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.springframework.stereotype.Component;

@Component
public class RecurrentPaymentToolCopyFactoryImpl implements MachineEventCopyFactory<RecurrentPaymentTool, Integer> {

    @Override
    public RecurrentPaymentTool create(MachineEvent event, Long sequenceId, Integer id, RecurrentPaymentTool old,
                                       String occurredAt) {
        RecurrentPaymentTool recurrentPaymentTool = null;
        if (old != null) {
            recurrentPaymentTool = new RecurrentPaymentTool(old);
        } else {
            recurrentPaymentTool = new RecurrentPaymentTool();
        }
        recurrentPaymentTool.setId(null);
        recurrentPaymentTool.setWtime(null);
        recurrentPaymentTool.setChangeId(id);
        recurrentPaymentTool.setSequenceId(sequenceId.intValue());
        recurrentPaymentTool.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        return recurrentPaymentTool;
    }

    @Override
    public RecurrentPaymentTool create(MachineEvent event, Long sequenceId, Integer id, String occurredAt) {
        return create(event, sequenceId, id, null, occurredAt);
    }

}
