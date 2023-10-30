package dev.vality.daway.handler.event.stock.impl.deposit;

import dev.vality.daway.handler.event.stock.Handler;
import dev.vality.fistful.deposit.TimestampedChange;
import dev.vality.machinegun.eventsink.MachineEvent;

public interface DepositHandler extends Handler<TimestampedChange, MachineEvent> {
}
