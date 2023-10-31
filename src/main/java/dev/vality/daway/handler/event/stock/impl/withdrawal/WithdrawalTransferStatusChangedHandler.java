package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.dao.withdrawal.iface.FistfulCashFlowDao;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalDao;
import dev.vality.daway.domain.enums.FistfulCashFlowChangeType;
import dev.vality.daway.domain.enums.WithdrawalTransferStatus;
import dev.vality.daway.domain.tables.pojos.FistfulCashFlow;
import dev.vality.daway.domain.tables.pojos.Withdrawal;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.fistful.transfer.Status;
import dev.vality.fistful.withdrawal.Change;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.geck.common.util.TBaseUtil;
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

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalTransferStatusChangedHandler implements WithdrawalHandler {

    private final WithdrawalDao withdrawalDao;
    private final FistfulCashFlowDao fistfulCashFlowDao;
    private final MachineEventCopyFactory<Withdrawal, String> machineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.transfer.payload.status_changed.status", new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        Status status = change.getTransfer().getPayload().getStatusChanged().getStatus();
        long sequenceId = event.getEventId();
        String withdrawalId = event.getSourceId();
        log.info("Start withdrawal transfer status changed handling, sequenceId={}, withdrawalId={} transfer={}",
                sequenceId, withdrawalId, change.getTransfer());

        final Withdrawal withdrawalOld = withdrawalDao.get(withdrawalId);
        Withdrawal withdrawalNew = machineEventCopyFactory
                .create(event, sequenceId, withdrawalId, withdrawalOld, timestampedChange.getOccuredAt());
        withdrawalNew.setWithdrawalTransferStatus(TBaseUtil.unionFieldToEnum(status, WithdrawalTransferStatus.class));

        withdrawalDao.save(withdrawalNew).ifPresentOrElse(
                id -> {
                    Long oldId = withdrawalOld.getId();
                    withdrawalDao.updateNotCurrent(oldId);
                    List<FistfulCashFlow> cashFlows =
                            fistfulCashFlowDao.getByObjId(oldId, FistfulCashFlowChangeType.withdrawal);
                    cashFlows.forEach(pcf -> {
                        pcf.setId(null);
                        pcf.setObjId(id);
                    });
                    fistfulCashFlowDao.save(cashFlows);
                    log.info("Withdrawal transfer status have been changed, sequenceId={}, withdrawalId={}", sequenceId,
                            withdrawalId);
                },
                () -> log.info("Withdrawal transfer have been changed, sequenceId={}, withdrawalId={}", sequenceId,
                        withdrawalId));
    }

}
