package dev.vality.daway.handler.event.stock.impl.deposit.adjustment;

import dev.vality.daway.dao.deposit.adjustment.iface.DepositAdjustmentDao;
import dev.vality.daway.dao.deposit.iface.DepositDao;
import dev.vality.daway.domain.enums.DepositAdjustmentStatus;
import dev.vality.daway.domain.enums.DepositStatus;
import dev.vality.daway.domain.tables.pojos.Deposit;
import dev.vality.daway.domain.tables.pojos.DepositAdjustment;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.handler.event.stock.impl.deposit.DepositHandler;
import dev.vality.daway.util.FistfulCashFlowUtil;
import dev.vality.fistful.deposit.Change;
import dev.vality.fistful.deposit.TimestampedChange;
import dev.vality.fistful.deposit.adjustment.CashFlowChangePlan;
import dev.vality.fistful.deposit.status.Status;
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
public class DepositAdjustmentCreatedHandler implements DepositHandler {

    private final DepositAdjustmentDao depositAdjustmentDao;
    private final DepositDao depositDao;
    private final MachineEventCopyFactory<DepositAdjustment, String> depositRevertMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.adjustment.payload.created.adjustment", new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        long sequenceId = event.getEventId();
        String depositId = event.getSourceId();
        log.info("Start deposit adjustment created handling, sequenceId={}, depositId={}", sequenceId, depositId);

        var adjustment = change.getAdjustment().getPayload().getCreated().getAdjustment();
        Deposit deposit = depositDao.get(depositId);
        DepositAdjustment depositAdjustment = depositRevertMachineEventCopyFactory
                .create(event, sequenceId, depositId, timestampedChange.getOccuredAt());

        depositAdjustment.setAdjustmentId(adjustment.getId());
        depositAdjustment.setWalletId(deposit.getWalletId());
        depositAdjustment.setSourceId(deposit.getSourceId());

        if (adjustment.getChangesPlan().isSetNewCashFlow()) {
            CashFlowChangePlan cashFlow = adjustment.getChangesPlan().getNewCashFlow();
            long amount = computeAmount(cashFlow);
            depositAdjustment.setAmount(amount);
            String currCode = getSymbolicCode(cashFlow);
            depositAdjustment.setCurrencyCode(currCode);
            depositAdjustment
                    .setProviderFee(FistfulCashFlowUtil.getFistfulProviderFee(cashFlow.getNewCashFlow().getPostings()));
            depositAdjustment.setFee(FistfulCashFlowUtil.getFistfulFee(cashFlow.getNewCashFlow().getPostings()));
        }
        if (adjustment.getChangesPlan().isSetNewStatus()) {
            Status status = adjustment.getChangesPlan().getNewStatus().getNewStatus();
            depositAdjustment.setDepositStatus(TBaseUtil.unionFieldToEnum(status, DepositStatus.class));
        }

        depositAdjustment.setStatus(DepositAdjustmentStatus.pending);
        depositAdjustment.setExternalId(adjustment.getExternalId());
        depositAdjustment.setPartyRevision(adjustment.getPartyRevision());
        depositAdjustment.setDomainRevision(adjustment.getDomainRevision());

        depositAdjustmentDao.save(depositAdjustment).ifPresentOrElse(
                dbContractId -> log.info("Deposit adjustment created has been saved, sequenceId={}, depositId={}",
                        sequenceId, depositId),
                () -> log.info("Deposit adjustment created bound duplicated, sequenceId={}, depositId={}",
                        sequenceId, depositId));
    }

    private String getSymbolicCode(CashFlowChangePlan cashFlow) {
        return cashFlow.getNewCashFlow().getPostings().get(0).getVolume().getCurrency().getSymbolicCode();
    }

    private long computeAmount(CashFlowChangePlan cashFlow) {
        Long oldAmount = FistfulCashFlowUtil.computeAmount(cashFlow.getOldCashFlowInverted().getPostings());
        Long newAmount = FistfulCashFlowUtil.computeAmount(cashFlow.getNewCashFlow().getPostings());
        return newAmount + oldAmount;
    }
}
