package dev.vality.daway.factory.machine.event;

import dev.vality.machinegun.eventsink.MachineEvent;

public interface MachineEventCopyFactory<T, K> {

    T create(MachineEvent event, Long sequenceId, K id, T copiedObject, String occurredAt);

    T create(MachineEvent event, Long sequenceId, K id, String occurredAt);

}
