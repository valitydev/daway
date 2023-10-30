package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.dao.withdrawal.iface.FistfulCashFlowDao;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalAdjustmentDao;
import dev.vality.daway.domain.enums.FistfulCashFlowChangeType;
import dev.vality.daway.domain.enums.WithdrawalTransferStatus;
import dev.vality.daway.domain.tables.pojos.FistfulCashFlow;
import dev.vality.daway.domain.tables.pojos.WithdrawalAdjustment;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.fistful.transfer.Status;
import dev.vality.fistful.withdrawal.Change;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.fistful.withdrawal.adjustment.TransferChange;
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

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalAdjustmentTransferStatusChangedHandler implements WithdrawalHandler, WithdrawalAdjustmentHandler {

    private final WithdrawalAdjustmentDao withdrawalAdjustmentDao;
    private final FistfulCashFlowDao fistfulCashFlowDao;
    private final MachineEventCopyFactory<WithdrawalAdjustment, String> machineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule(
                    "change.adjustment.payload.transfer.payload.status_changed.status",
                    new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        TransferChange transferChange = change.getAdjustment().getPayload().getTransfer();
        Status status = transferChange.getPayload().getStatusChanged().getStatus();
        long sequenceId = event.getEventId();
        String withdrawalId = event.getSourceId();
        String withdrawalAdjustmentId = change.getAdjustment().getId();
        log.info("Start withdrawal adjustment transfer status changed handling, sequenceId={}, withdrawalId={}, withdrawalAdjustmentId={}, transfer={}",
                sequenceId, withdrawalId, withdrawalAdjustmentId, transferChange);

        final WithdrawalAdjustment withdrawalAdjustmentOld = withdrawalAdjustmentDao.getByIds(withdrawalId, withdrawalAdjustmentId);
        WithdrawalAdjustment withdrawalAdjustmentNew = machineEventCopyFactory
                .create(event, sequenceId, withdrawalAdjustmentId, withdrawalAdjustmentOld, timestampedChange.getOccuredAt());
        withdrawalAdjustmentNew.setWithdrawalTransferStatus(TBaseUtil.unionFieldToEnum(status, WithdrawalTransferStatus.class));

        withdrawalAdjustmentDao.save(withdrawalAdjustmentNew).ifPresentOrElse(
                id -> {
                    Long oldId = withdrawalAdjustmentOld.getId();
                    log.info("Update not current for withdrawal adjustment with id={}", oldId);
                    withdrawalAdjustmentDao.updateNotCurrent(oldId);
                    List<FistfulCashFlow> cashFlows =
                            fistfulCashFlowDao.getByObjId(oldId, FistfulCashFlowChangeType.withdrawal_adjustment);
                    cashFlows.forEach(pcf -> {
                        pcf.setId(null);
                        pcf.setObjId(id);
                    });
                    fistfulCashFlowDao.save(cashFlows);
                    log.info("Withdrawal adjustment transfer status have been changed, sequenceId={}, withdrawalId={}, withdrawalAdjustmentId={}",
                            sequenceId, withdrawalId, withdrawalAdjustmentId);
                },
                () -> log.info("Withdrawal adjustment transfer have been changed, sequenceId={}, withdrawalId={}, withdrawalAdjustmentId={}",
                        sequenceId, withdrawalId, withdrawalAdjustmentId));
    }

}
