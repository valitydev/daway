package dev.vality.daway.handler.event.stock.impl.payout;

import dev.vality.daway.dao.payout.iface.PayoutDao;
import dev.vality.daway.domain.enums.PayoutStatus;
import dev.vality.daway.domain.tables.pojos.Payout;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.payout.manager.Event;
import dev.vality.payout.manager.PayoutChange;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutCreatedHandler implements PayoutHandler {

    private final PayoutDao payoutDao;

    @Getter
    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "created",
            new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PayoutChange change, Event event) {
        var payoutSource = change.getCreated().getPayout();
        String payoutId = payoutSource.getPayoutId();
        String partyId = payoutSource.getPartyId();
        log.info("Start payout created handling, sequenceId={}, partyId={}, payoutId={}",
                event.getSequenceId(), partyId, payoutId);
        Payout payout = new Payout();
        payout.setSequenceId(event.getSequenceId());
        payout.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        payout.setPayoutId(payoutId);
        payout.setPartyId(partyId);
        payout.setShopId(payoutSource.getShopId());
        payout.setCreatedAt(TypeUtil.stringToLocalDateTime(payoutSource.getCreatedAt()));
        payout.setStatus(TBaseUtil.unionFieldToEnum(payoutSource.getStatus(), PayoutStatus.class));
        payout.setAmount(payoutSource.getAmount());
        payout.setFee(payoutSource.getFee());
        payout.setPayoutToolId(payoutSource.getPayoutToolId());
        payout.setCurrencyCode(payoutSource.getCurrency().getSymbolicCode());

        Optional<Long> id = payoutDao.save(payout);
        if (id.isEmpty()) {
            log.info("Payout has been bound duplicated, sequenceId={}, changeId={}, payoutId={}",
                    event.getSequenceId(), partyId, payoutId);
        }
    }
}
