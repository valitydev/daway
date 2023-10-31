package dev.vality.daway.handler.event.stock.impl.deposit.adjustment;

import dev.vality.daway.dao.deposit.adjustment.iface.DepositAdjustmentDao;
import dev.vality.daway.dao.withdrawal.iface.FistfulCashFlowDao;
import dev.vality.daway.domain.enums.DepositTransferStatus;
import dev.vality.daway.domain.enums.FistfulCashFlowChangeType;
import dev.vality.daway.domain.tables.pojos.DepositAdjustment;
import dev.vality.daway.domain.tables.pojos.FistfulCashFlow;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.deposit.DepositHandler;
import dev.vality.daway.util.FistfulCashFlowUtil;
import dev.vality.fistful.cashflow.FinalCashFlowPosting;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositAdjustmentTransferCreatedHandler implements DepositHandler {

    private final DepositAdjustmentDao depositAdjustmentDao;
    private final FistfulCashFlowDao fistfulCashFlowDao;
    private final MachineEventCopyFactory<DepositAdjustment, String> depositRevertMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.adjustment.payload.transfer.payload.created", new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        long sequenceId = event.getEventId();
        String depositId = event.getSourceId();
        String adjustmentId = change.getAdjustment().getId();
        log.info("Start deposit adjustment transfer created handling, sequenceId={}, depositId={}", sequenceId,
                depositId);
        DepositAdjustment depositAdjustmentOld = depositAdjustmentDao.get(depositId, adjustmentId);
        DepositAdjustment depositAdjustmentNew = depositRevertMachineEventCopyFactory
                .create(event, sequenceId, depositId, depositAdjustmentOld, timestampedChange.getOccuredAt());

        List<FinalCashFlowPosting> postings =
                change.getAdjustment().getPayload().getTransfer().getPayload().getCreated().getTransfer().getCashflow()
                        .getPostings();
        depositAdjustmentNew.setTransferStatus(DepositTransferStatus.created);
        depositAdjustmentNew.setFee(FistfulCashFlowUtil.getFistfulFee(postings));
        depositAdjustmentNew.setProviderFee(FistfulCashFlowUtil.getFistfulProviderFee(postings));

        depositAdjustmentDao.save(depositAdjustmentNew).ifPresentOrElse(
                id -> {
                    depositAdjustmentDao.updateNotCurrent(depositAdjustmentOld.getId());
                    List<FistfulCashFlow> fistfulCashFlows = FistfulCashFlowUtil
                            .convertFistfulCashFlows(postings, id, FistfulCashFlowChangeType.deposit_adjustment);
                    fistfulCashFlowDao.save(fistfulCashFlows);
                },
                () -> log.info("Deposit adjustment transfer have been saved, sequenceId={}, depositId={}", sequenceId,
                        depositId)
        );
    }
}
