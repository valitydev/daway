package dev.vality.daway.handler.event.stock.impl.withdrawal;

import dev.vality.daway.dao.withdrawal.iface.WithdrawalAdjustmentDao;
import dev.vality.daway.domain.enums.WithdrawalAdjustmentStatus;
import dev.vality.daway.domain.tables.pojos.WithdrawalAdjustment;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.fistful.withdrawal.AdjustmentChange;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.fistful.withdrawal.adjustment.Status;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalAdjustmentStatusChangedHandler implements WithdrawalHandler, WithdrawalAdjustmentHandler {

    private final WithdrawalAdjustmentDao withdrawalAdjustmentDao;
    private final MachineEventCopyFactory<WithdrawalAdjustment, String> machineEventCopyFactory;

    @Getter
    private final Filter filter =
            new PathConditionFilter(new PathConditionRule("change.adjustment.payload.status_changed.status", new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        AdjustmentChange adjustmentChange = timestampedChange.getChange().getAdjustment();
        Status status = adjustmentChange.getPayload().getStatusChanged().getStatus();
        long sequenceId = event.getEventId();
        String withdrawalId = event.getSourceId();
        String withdrawalAdjustmentId = adjustmentChange.getId();
        log.info("Start withdrawal adjustment status changed handling, " +
                        "sequenceId={}, withdrawalId={}, withdrawalAdjustmentId={}",
                sequenceId, withdrawalId, withdrawalAdjustmentId);
        final var withdrawalAdjustmentOld = withdrawalAdjustmentDao.getByIds(withdrawalId, withdrawalAdjustmentId);
        var withdrawalAdjustmentNew = machineEventCopyFactory
                .create(event, sequenceId, withdrawalAdjustmentId, withdrawalAdjustmentOld, timestampedChange.getOccuredAt());
        withdrawalAdjustmentNew.setStatus(TBaseUtil.unionFieldToEnum(status, WithdrawalAdjustmentStatus.class));
        withdrawalAdjustmentDao.save(withdrawalAdjustmentNew).ifPresentOrElse(
                id -> {
                    Long oldId = withdrawalAdjustmentOld.getId();
                    log.info("Update not current for withdrawal adjustment with id={}", oldId);
                    withdrawalAdjustmentDao.updateNotCurrent(oldId);
                    log.info("WithdrawalAdjustment status have been changed, sequenceId={}, withdrawalId={}, withdrawalAdjustmentId={}",
                            sequenceId, withdrawalId, withdrawalAdjustmentId);
                },
                () -> log.info("WithdrawalAdjustment status have been changed, sequenceId={}, withdrawalId={}, withdrawalAdjustmentId={}",
                        sequenceId, withdrawalId, withdrawalAdjustmentId));
    }

}
