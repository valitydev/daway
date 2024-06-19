package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.dao.withdrawal.iface.WithdrawalDao;
import dev.vality.daway.domain.enums.WithdrawalStatus;
import dev.vality.daway.domain.tables.pojos.Withdrawal;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.service.ExchangeRateCalculationService;
import dev.vality.fistful.base.Cash;
import dev.vality.fistful.withdrawal.Change;
import dev.vality.fistful.withdrawal.TimestampedChange;
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
public class WithdrawalCreatedHandler implements WithdrawalHandler {

    private final WithdrawalDao withdrawalDao;
    private final MachineEventCopyFactory<Withdrawal, String> machineEventCopyFactory;
    private final ExchangeRateCalculationService exchangeRateCalculationService;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.created.withdrawal", new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        var withdrawalDamsel = change.getCreated().getWithdrawal();
        long sequenceId = event.getEventId();
        String withdrawalId = event.getSourceId();
        log.info("Start withdrawal created handling, sequenceId={}, withdrawalId={}", sequenceId, withdrawalId);

        Withdrawal withdrawal =
                machineEventCopyFactory.create(event, sequenceId, withdrawalId, timestampedChange.getOccuredAt());

        withdrawal.setWalletId(withdrawalDamsel.getWalletId());
        withdrawal.setDestinationId(withdrawalDamsel.getDestinationId());
        withdrawal.setExternalId(withdrawalDamsel.getExternalId());

        Cash cash = withdrawalDamsel.getBody();
        withdrawal.setAmount(cash.getAmount());
        withdrawal.setCurrencyCode(cash.getCurrency().getSymbolicCode());
        withdrawal.setWithdrawalStatus(WithdrawalStatus.pending);
        if (withdrawalDamsel.getRoute() != null && withdrawalDamsel.getRoute().isSetTerminalId()) {
            withdrawal.setTerminalId(String.valueOf(withdrawalDamsel.getRoute().getTerminalId()));
        }
        if (withdrawalDamsel.isSetQuote()) {
            long amountFrom = withdrawalDamsel.getQuote().getCashFrom().getAmount();
            long amountTo = withdrawalDamsel.getQuote().getCashTo().getAmount();
            var exchangeRate = exchangeRateCalculationService.calculate(amountFrom, amountTo);
            withdrawal.setExchangeRate(exchangeRate.floatValue());
        }

        withdrawalDao.save(withdrawal).ifPresentOrElse(
                dbContractId -> log
                        .info("Withdrawal created has been saved, sequenceId={}, withdrawalId={}", sequenceId,
                                withdrawalId),
                () -> log.info("Withdrawal created bound duplicated, sequenceId={}, withdrawalId={}", sequenceId,
                        withdrawalId));
    }
}
