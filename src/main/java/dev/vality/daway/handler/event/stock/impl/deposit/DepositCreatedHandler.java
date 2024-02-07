package dev.vality.daway.handler.event.stock.impl.deposit;

import dev.vality.daway.dao.deposit.iface.DepositDao;
import dev.vality.daway.domain.enums.DepositStatus;
import dev.vality.daway.domain.tables.pojos.Deposit;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.fistful.base.Cash;
import dev.vality.fistful.deposit.Change;
import dev.vality.fistful.deposit.TimestampedChange;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositCreatedHandler implements DepositHandler {

    private final DepositDao depositDao;
    private final MachineEventCopyFactory<Deposit, String> depositMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.created.deposit", new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        long sequenceId = event.getEventId();
        String depositId = event.getSourceId();
        log.info("Start deposit created handling, sequenceId={}, depositId={}", sequenceId, depositId);
        Deposit deposit =
                depositMachineEventCopyFactory.create(event, sequenceId, depositId, timestampedChange.getOccuredAt());

        var depositDamsel = change.getCreated().getDeposit();
        deposit.setSourceId(depositDamsel.getSourceId());
        deposit.setWalletId(depositDamsel.getWalletId());
        Cash cash = depositDamsel.getBody();
        deposit.setAmount(cash.getAmount());
        deposit.setCurrencyCode(cash.getCurrency().getSymbolicCode());
        deposit.setDepositStatus(DepositStatus.pending);
        deposit.setExternalId(depositDamsel.getExternalId());
        deposit.setDescription(depositDamsel.getDescription());

        depositDao.save(deposit).ifPresentOrElse(
                dbContractId -> log.info("Deposit created has been saved, sequenceId={}, depositId={}",
                        sequenceId, depositId),
                () -> log.info("Deposit created bound duplicated, sequenceId={}, depositId={}",
                        sequenceId, depositId));
    }

}
