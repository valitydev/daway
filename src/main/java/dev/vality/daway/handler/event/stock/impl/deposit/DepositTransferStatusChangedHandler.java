package dev.vality.daway.handler.event.stock.impl.deposit;

import dev.vality.daway.dao.deposit.iface.DepositDao;
import dev.vality.daway.dao.withdrawal.iface.FistfulCashFlowDao;
import dev.vality.daway.domain.enums.DepositTransferStatus;
import dev.vality.daway.domain.enums.FistfulCashFlowChangeType;
import dev.vality.daway.domain.tables.pojos.Deposit;
import dev.vality.daway.domain.tables.pojos.FistfulCashFlow;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.fistful.deposit.Change;
import dev.vality.fistful.deposit.TimestampedChange;
import dev.vality.fistful.transfer.Status;
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
public class DepositTransferStatusChangedHandler implements DepositHandler {

    private final DepositDao depositDao;
    private final FistfulCashFlowDao fistfulCashFlowDao;
    private final MachineEventCopyFactory<Deposit, String> depositMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.transfer.payload.status_changed.status", new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        Status status = change.getTransfer().getPayload().getStatusChanged().getStatus();
        long sequenceId = event.getEventId();
        String depositId = event.getSourceId();
        log.info("Start deposit transfer status changed handling, sequenceId={}, depositId={}", sequenceId, depositId);
        final Deposit depositOld = depositDao.get(depositId);
        Deposit depositNew = depositMachineEventCopyFactory
                .create(event, sequenceId, depositId, depositOld, timestampedChange.getOccuredAt());

        depositNew.setDepositTransferStatus(TBaseUtil.unionFieldToEnum(status, DepositTransferStatus.class));

        depositDao.save(depositNew).ifPresentOrElse(
                id -> {
                    Long oldId = depositOld.getId();
                    depositDao.updateNotCurrent(oldId);
                    List<FistfulCashFlow> cashFlows =
                            fistfulCashFlowDao.getByObjId(oldId, FistfulCashFlowChangeType.deposit);
                    cashFlows.forEach(pcf -> {
                        pcf.setId(null);
                        pcf.setObjId(id);
                    });
                    fistfulCashFlowDao.save(cashFlows);
                },
                () -> log.info("Deposit transfer status have been changed, sequenceId={}, depositId={}",
                        sequenceId, depositId)
        );
    }

}
