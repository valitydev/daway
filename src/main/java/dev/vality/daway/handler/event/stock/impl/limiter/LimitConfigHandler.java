package dev.vality.daway.handler.event.stock.impl.limiter;

import dev.vality.daway.handler.event.stock.Handler;
import dev.vality.limiter.config.TimestampedChange;
import dev.vality.machinegun.eventsink.MachineEvent;

public interface LimitConfigHandler extends Handler<TimestampedChange, MachineEvent> {
}
