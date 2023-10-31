package dev.vality.daway.handler.event.stock.impl.wallet;

import dev.vality.daway.handler.event.stock.Handler;
import dev.vality.fistful.wallet.TimestampedChange;
import dev.vality.machinegun.eventsink.MachineEvent;

public interface WalletHandler extends Handler<TimestampedChange, MachineEvent> {
}
