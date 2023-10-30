package dev.vality.daway.handler.event.stock.impl.payout;

import dev.vality.daway.dao.payout.iface.PayoutDao;
import dev.vality.daway.domain.tables.pojos.Payout;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.payout.manager.Event;
import dev.vality.payout.manager.PayoutChange;
import dev.vality.payout.manager.PayoutStatus;
import dev.vality.payout.manager.PayoutStatusChanged;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutStatusChangedHandler implements PayoutHandler {

    private final PayoutDao payoutDao;

    @Getter
    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "status_changed",
            new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PayoutChange change, Event event) {
        String payoutId = event.getPayoutId();
        log.info("Start payout status changed handling, sequenceId={}, payoutId={}", event.getSequenceId(), payoutId);
        var payoutSourceOld = payoutDao.get(payoutId);
        var payoutNew = new Payout(payoutSourceOld);
        payoutNew.setId(null);
        payoutNew.setWtime(null);
        payoutNew.setSequenceId(event.getSequenceId());
        payoutNew.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        PayoutStatusChanged payoutStatusChanged = change.getStatusChanged();
        PayoutStatus payoutStatus = payoutStatusChanged.getStatus();
        payoutNew.setStatus(
                TBaseUtil.unionFieldToEnum(payoutStatus, dev.vality.daway.domain.enums.PayoutStatus.class));

        if (payoutStatus.isSetCancelled()) {
            payoutNew.setCancelledDetails(payoutStatus.getCancelled().getDetails());
        }

        payoutDao.save(payoutNew).ifPresentOrElse(
                id -> {
                    Long oldId = payoutSourceOld.getId();
                    payoutDao.updateNotCurrent(oldId);
                    log.info("Payout status  has been saved, sequenceId={}, payoutId={}",
                            event.getSequenceId(), payoutId);
                },
                () -> log.info("Payout status  bound duplicated, sequenceId={}, payoutId={}",
                        event.getSequenceId(), payoutId)
        );
    }

}
