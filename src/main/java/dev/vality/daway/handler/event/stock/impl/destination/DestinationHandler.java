package dev.vality.daway.handler.event.stock.impl.destination;

import dev.vality.daway.handler.event.stock.Handler;
import dev.vality.fistful.destination.TimestampedChange;
import dev.vality.machinegun.eventsink.MachineEvent;

public interface DestinationHandler extends Handler<TimestampedChange, MachineEvent> {
}
