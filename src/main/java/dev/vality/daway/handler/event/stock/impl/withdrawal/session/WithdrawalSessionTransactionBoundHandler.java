package dev.vality.daway.handler.event.stock.impl.withdrawal.session;

import dev.vality.daway.dao.withdrawal.session.iface.WithdrawalSessionDao;
import dev.vality.daway.domain.tables.pojos.WithdrawalSession;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.util.JsonUtil;
import dev.vality.fistful.base.TransactionInfo;
import dev.vality.fistful.withdrawal_session.Change;
import dev.vality.fistful.withdrawal_session.TimestampedChange;
import dev.vality.fistful.withdrawal_session.TransactionBoundChange;
import dev.vality.geck.common.util.TypeUtil;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalSessionTransactionBoundHandler implements WithdrawalSessionHandler {

    private final WithdrawalSessionDao withdrawalSessionDao;
    private final MachineEventCopyFactory<WithdrawalSession, String> withdrawalSessionMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.transaction_bound", new IsNullCondition().not())
    );

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        long sequenceId = event.getEventId();
        String withdrawalSessionId = event.getSourceId();
        log.info("Start withdrawal transaction bound handling, sequenceId={}, withdrawalSessionId={}",
                sequenceId, withdrawalSessionId);

        final WithdrawalSession withdrawalSessionOld = withdrawalSessionDao.get(withdrawalSessionId);
        WithdrawalSession withdrawalSessionNew = withdrawalSessionMachineEventCopyFactory
                .create(event, sequenceId, withdrawalSessionId, withdrawalSessionOld, timestampedChange.getOccuredAt());

        TransactionBoundChange transactionBound = change.getTransactionBound();
        TransactionInfo trxInfo = transactionBound.getTrxInfo();
        withdrawalSessionNew.setTranInfoId(trxInfo.getId());
        if (trxInfo.isSetTimestamp()) {
            withdrawalSessionNew.setTranInfoTimestamp(TypeUtil.stringToLocalDateTime(trxInfo.getTimestamp()));
        }
        withdrawalSessionNew.setTranInfoJson(JsonUtil.objectToJsonString(trxInfo.getExtra()));
        if (trxInfo.isSetAdditionalInfo()) {
            withdrawalSessionNew.setTranAdditionalInfoRrn(trxInfo.getAdditionalInfo().getRrn());
            withdrawalSessionNew
                    .setTranAdditionalInfoJson(JsonUtil.thriftBaseToJsonString(trxInfo.getAdditionalInfo()));
        }

        withdrawalSessionDao.save(withdrawalSessionNew).ifPresentOrElse(
                id -> {
                    withdrawalSessionDao.updateNotCurrent(withdrawalSessionOld.getId());
                    log.info("Withdrawal session transaction bound have been changed, sequenceId={}, " +
                                    "withdrawalSessionId={}, WithdrawalSessionStatus={}", sequenceId,
                            withdrawalSessionId, withdrawalSessionOld.getWithdrawalSessionStatus());
                },
                () -> log.info("Withdrawal session transaction bound have been changed, sequenceId={}, " +
                                "withdrawalSessionId={}, WithdrawalSessionStatus={}", sequenceId,
                        withdrawalSessionId, withdrawalSessionOld.getWithdrawalSessionStatus()));

    }
}
