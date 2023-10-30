package dev.vality.daway.handler.event.stock.impl.deposit.revert;

import dev.vality.daway.dao.deposit.revert.iface.DepositRevertDao;
import dev.vality.daway.dao.withdrawal.iface.FistfulCashFlowDao;
import dev.vality.daway.domain.enums.DepositTransferStatus;
import dev.vality.daway.domain.enums.FistfulCashFlowChangeType;
import dev.vality.daway.domain.tables.pojos.DepositRevert;
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
public class DepositRevertTransferCreatedHandler implements DepositHandler {

    private final DepositRevertDao depositRevertDao;
    private final FistfulCashFlowDao fistfulCashFlowDao;
    private final MachineEventCopyFactory<DepositRevert, String> depositRevertMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.revert.payload.transfer.payload.created", new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        long sequenceId = event.getEventId();
        String depositId = event.getSourceId();
        String revertId = change.getRevert().getId();
        log.info("Start deposit revert transfer created handling, sequenceId={}, depositId={}", sequenceId, depositId);
        final DepositRevert depositRevertOld = depositRevertDao.get(depositId, revertId);
        DepositRevert depositRevertNew = depositRevertMachineEventCopyFactory
                .create(event, sequenceId, depositId, depositRevertOld, timestampedChange.getOccuredAt());

        List<FinalCashFlowPosting> postings =
                change.getRevert().getPayload().getTransfer().getPayload().getCreated().getTransfer().getCashflow()
                        .getPostings();
        depositRevertNew.setTransferStatus(DepositTransferStatus.created);
        depositRevertNew.setFee(FistfulCashFlowUtil.getFistfulFee(postings));
        depositRevertNew.setProviderFee(FistfulCashFlowUtil.getFistfulProviderFee(postings));

        depositRevertDao.save(depositRevertNew).ifPresentOrElse(
                id -> {
                    depositRevertDao.updateNotCurrent(depositRevertOld.getId());
                    List<FistfulCashFlow> fistfulCashFlows = FistfulCashFlowUtil
                            .convertFistfulCashFlows(postings, id, FistfulCashFlowChangeType.deposit_revert);
                    fistfulCashFlowDao.save(fistfulCashFlows);
                },
                () -> log.info("Deposit revert transfer have been saved, sequenceId={}, depositId={}", sequenceId,
                        depositId)
        );
    }

}
