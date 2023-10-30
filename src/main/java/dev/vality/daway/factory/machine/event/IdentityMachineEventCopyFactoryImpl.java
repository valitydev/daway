package dev.vality.daway.factory.machine.event;

import dev.vality.daway.domain.tables.pojos.Identity;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import org.springframework.stereotype.Component;

@Component
public class IdentityMachineEventCopyFactoryImpl implements MachineEventCopyFactory<Identity, String> {

    @Override
    public Identity create(MachineEvent event, Long sequenceId, String id, Identity old, String occurredAt) {
        Identity identity = null;
        if (old != null) {
            identity = new Identity(old);
        } else {
            identity = new Identity();
        }
        identity.setId(null);
        identity.setWtime(null);
        identity.setIdentityId(id);
        identity.setSequenceId(sequenceId.intValue());
        identity.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        identity.setEventOccuredAt(TypeUtil.stringToLocalDateTime(occurredAt));
        return identity;
    }

    @Override
    public Identity create(MachineEvent event, Long sequenceId, String id, String occurredAt) {
        return create(event, sequenceId, id, null, occurredAt);
    }

}
