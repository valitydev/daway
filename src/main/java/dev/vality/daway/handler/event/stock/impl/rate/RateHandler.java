package dev.vality.daway.handler.event.stock.impl.rate;

import dev.vality.daway.handler.event.stock.Handler;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.xrates.rate.Change;

public interface RateHandler extends Handler<Change, MachineEvent> {
}
