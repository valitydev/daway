package dev.vality.daway.handler.event.stock.impl.source;

import dev.vality.daway.handler.event.stock.Handler;
import dev.vality.fistful.source.TimestampedChange;
import dev.vality.machinegun.eventsink.MachineEvent;

public interface SourceHandler extends Handler<TimestampedChange, MachineEvent> {

}
