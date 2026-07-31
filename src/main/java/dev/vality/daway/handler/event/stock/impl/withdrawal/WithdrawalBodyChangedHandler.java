package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.dao.withdrawal.iface.WithdrawalCashChangeDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalCashChange;
import dev.vality.fistful.base.Cash;
import dev.vality.fistful.withdrawal.BodyChange;
import dev.vality.fistful.withdrawal.Change;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalBodyChangedHandler implements WithdrawalHandler {

    private final WithdrawalCashChangeDao withdrawalCashChangeDao;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.body_changed", new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        long sequenceId = event.getEventId();
        String withdrawalId = event.getSourceId();
        log.info("Start withdrawal body changed handling, sequenceId={}, withdrawalId={}", sequenceId, withdrawalId);

        BodyChange bodyChange = change.getBodyChanged();
        WithdrawalCashChange cashChange = new WithdrawalCashChange();
        cashChange.setWtime(null);
        cashChange.setId(null);
        cashChange.setSequenceId(sequenceId);
        cashChange.setWithdrawalId(withdrawalId);
        cashChange.setCurrent(true);
        cashChange.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));

        Cash newCash = bodyChange.getNewBody();
        cashChange.setNewAmount(newCash.getAmount());
        cashChange.setNewCurrencyCode(newCash.getCurrency().getSymbolicCode());
        Cash oldCash = bodyChange.getOldBody();
        cashChange.setOldAmount(oldCash.getAmount());
        cashChange.setOldCurrencyCode(oldCash.getCurrency().getSymbolicCode());

        withdrawalCashChangeDao.save(cashChange);
        withdrawalCashChangeDao.switchCurrent(withdrawalId);
        log.info("Withdrawal body change has been saved, sequenceId={}, withdrawalId={}", sequenceId, withdrawalId);
    }

}
