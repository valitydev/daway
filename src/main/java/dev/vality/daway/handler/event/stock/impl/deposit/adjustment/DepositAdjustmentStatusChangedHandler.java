package dev.vality.daway.handler.event.stock.impl.deposit.adjustment;

import dev.vality.daway.dao.deposit.adjustment.iface.DepositAdjustmentDao;
import dev.vality.daway.dao.withdrawal.iface.FistfulCashFlowDao;
import dev.vality.daway.domain.enums.DepositAdjustmentStatus;
import dev.vality.daway.domain.enums.FistfulCashFlowChangeType;
import dev.vality.daway.domain.tables.pojos.DepositAdjustment;
import dev.vality.daway.domain.tables.pojos.FistfulCashFlow;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.deposit.DepositHandler;
import dev.vality.fistful.deposit.Change;
import dev.vality.fistful.deposit.TimestampedChange;
import dev.vality.fistful.deposit.adjustment.Status;
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
public class DepositAdjustmentStatusChangedHandler implements DepositHandler {

    private final DepositAdjustmentDao depositAdjustmentDao;
    private final FistfulCashFlowDao fistfulCashFlowDao;
    private final MachineEventCopyFactory<DepositAdjustment, String> depositRevertMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.adjustment.payload.status_changed.status", new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        Status status = change.getAdjustment().getPayload().getStatusChanged().getStatus();
        long sequenceId = event.getEventId();
        String depositId = event.getSourceId();
        String adjustmentId = change.getAdjustment().getId();
        log.info("Start deposit adjustment status changed handling, sequenceId={}, depositId={}", sequenceId,
                depositId);
        DepositAdjustment depositAdjustmentOld = depositAdjustmentDao.get(depositId, adjustmentId);
        DepositAdjustment depositAdjustmentNew = depositRevertMachineEventCopyFactory
                .create(event, sequenceId, depositId, depositAdjustmentOld, timestampedChange.getOccuredAt());

        depositAdjustmentNew.setStatus(TBaseUtil.unionFieldToEnum(status, DepositAdjustmentStatus.class));

        depositAdjustmentDao.save(depositAdjustmentNew).ifPresentOrElse(
                id -> {
                    Long oldId = depositAdjustmentOld.getId();
                    depositAdjustmentDao.updateNotCurrent(oldId);
                    List<FistfulCashFlow> cashFlows = fistfulCashFlowDao
                            .getByObjId(oldId, FistfulCashFlowChangeType.deposit_adjustment);
                    cashFlows.forEach(pcf -> {
                        pcf.setId(null);
                        pcf.setObjId(id);
                    });
                    fistfulCashFlowDao.save(cashFlows);
                },
                () -> log.info("Deposit adjustment status have been changed, sequenceId={}, depositId={}",
                        sequenceId, depositId)
        );
    }

}
