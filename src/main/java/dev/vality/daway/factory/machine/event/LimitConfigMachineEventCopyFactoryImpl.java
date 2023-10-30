package dev.vality.daway.factory.machine.event;

import dev.vality.daway.domain.tables.pojos.LimitConfig;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.springframework.stereotype.Component;

@Component
public class LimitConfigMachineEventCopyFactoryImpl implements MachineEventCopyFactory<LimitConfig, String> {

    @Override
    public LimitConfig create(MachineEvent event, Long sequenceId, String id, LimitConfig old, String occurredAt) {
        var limitConfig = old != null ? new LimitConfig(old) : new LimitConfig();
        limitConfig.setId(null);
        limitConfig.setWtime(null);
        limitConfig.setSequenceId(sequenceId.intValue());
        limitConfig.setSourceId(id);
        limitConfig.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        limitConfig.setEventOccuredAt(TypeUtil.stringToLocalDateTime(occurredAt));
        return limitConfig;
    }

    @Override
    public LimitConfig create(MachineEvent event, Long sequenceId, String id, String occurredAt) {
        return create(event, sequenceId, id, null, occurredAt);
    }
}
