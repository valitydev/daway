package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.dao.withdrawal.iface.FistfulCashFlowDao;
import dev.vality.daway.dao.withdrawal.iface.WithdrawalAdjustmentDao;
import dev.vality.daway.domain.enums.FistfulCashFlowChangeType;
import dev.vality.daway.domain.enums.WithdrawalTransferStatus;
import dev.vality.daway.domain.tables.pojos.FistfulCashFlow;
import dev.vality.daway.domain.tables.pojos.WithdrawalAdjustment;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.util.FistfulCashFlowUtil;
import dev.vality.fistful.cashflow.FinalCashFlowPosting;
import dev.vality.fistful.withdrawal.Change;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.fistful.withdrawal.adjustment.TransferChange;
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
public class WithdrawalAdjustmentTransferCreatedHandler implements WithdrawalHandler, WithdrawalAdjustmentHandler {

    private final WithdrawalAdjustmentDao withdrawalAdjustmentDao;
    private final FistfulCashFlowDao fistfulCashFlowDao;
    private final MachineEventCopyFactory<WithdrawalAdjustment, String> machineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule(
                    "change.adjustment.payload.transfer.payload.created",
                    new IsNullCondition().not())
    );

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        long sequenceId = event.getEventId();
        String withdrawalId = event.getSourceId();
        String withdrawalAdjustmentId = change.getAdjustment().getId();
        TransferChange transferChange = change.getAdjustment().getPayload().getTransfer();
        log.info("Start withdrawal adjustment transfer created handling, " +
                        "sequenceId={}, withdrawalId={}, withdrawalAdjustmentId={}",
                sequenceId, withdrawalId, withdrawalAdjustmentId);
        log.debug("Transfer={}", transferChange);
        final var withdrawalAdjustmentOld = withdrawalAdjustmentDao.getByIds(withdrawalId, withdrawalAdjustmentId);
        var withdrawalAdjustmentNew = machineEventCopyFactory
                .create(event, sequenceId, withdrawalAdjustmentId, withdrawalAdjustmentOld, timestampedChange.getOccuredAt());

        withdrawalAdjustmentNew.setWithdrawalTransferStatus(WithdrawalTransferStatus.created);
        List<FinalCashFlowPosting> postings =
                transferChange.getPayload().getCreated().getTransfer().getCashflow().getPostings();
        withdrawalAdjustmentNew.setFee(FistfulCashFlowUtil.getFistfulFee(postings));
        withdrawalAdjustmentNew.setProviderFee(FistfulCashFlowUtil.getFistfulProviderFee(postings));

        withdrawalAdjustmentDao.save(withdrawalAdjustmentNew).ifPresentOrElse(
                id -> {
                    Long oldId = withdrawalAdjustmentOld.getId();
                    log.info("Update not current for withdrawal adjustment with id={}", oldId);
                    withdrawalAdjustmentDao.updateNotCurrent(oldId);
                    List<FistfulCashFlow> fistfulCashFlows = FistfulCashFlowUtil
                            .convertFistfulCashFlows(postings, id, FistfulCashFlowChangeType.withdrawal_adjustment);
                    fistfulCashFlowDao.save(fistfulCashFlows);
                    log.info("Withdrawal adjustment transfer have been changed, sequenceId={}, withdrawalId={}, withdrawalAdjustmentId={}",
                            sequenceId, withdrawalId, withdrawalAdjustmentId);
                },
                () -> log.info("Withdrawal adjustment transfer have been changed, sequenceId={}, withdrawalId={}, withdrawalAdjustmentId={}",
                        sequenceId, withdrawalId, withdrawalAdjustmentId));
    }

}
