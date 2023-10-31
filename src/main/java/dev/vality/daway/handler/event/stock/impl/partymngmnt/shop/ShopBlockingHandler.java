package dev.vality.daway.handler.event.stock.impl.partymngmnt.shop;

import dev.vality.damsel.domain.Blocking;
import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.daway.dao.party.iface.ShopDao;
import dev.vality.daway.domain.tables.pojos.Shop;
import dev.vality.daway.factory.claim.effect.ClaimEffectCopyFactory;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.PartyManagementHandler;
import dev.vality.geck.common.util.TBaseUtil;
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
public class ShopBlockingHandler implements PartyManagementHandler {

    private final ShopDao shopDao;
    private final ClaimEffectCopyFactory<Shop, Integer> claimEffectCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "shop_blocking",
            new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        Blocking blocking = change.getShopBlocking().getBlocking();
        String shopId = change.getShopBlocking().getShopId();
        String partyId = event.getSourceId();
        log.info("Start shop blocking handling, sequenceId={}, partyId={}, shopId={}, changeId={}",
                sequenceId, partyId, shopId, changeId);

        final Shop shopOld = shopDao.get(partyId, shopId);
        Shop shopNew = claimEffectCopyFactory.create(event, sequenceId, -1, changeId, shopOld);

        initBlockingFields(blocking, shopNew);

        shopDao.saveWithUpdateCurrent(shopNew, shopOld.getId(), "blocking");
    }

    private void initBlockingFields(Blocking blocking, Shop shopSource) {
        shopSource.setBlocking(TBaseUtil.unionFieldToEnum(blocking, dev.vality.daway.domain.enums.Blocking.class));
        if (blocking.isSetUnblocked()) {
            shopSource.setBlockingUnblockedReason(blocking.getUnblocked().getReason());
            shopSource.setBlockingUnblockedSince(TypeUtil.stringToLocalDateTime(blocking.getUnblocked().getSince()));
            shopSource.setBlockingBlockedReason(null);
            shopSource.setBlockingBlockedSince(null);
        } else if (blocking.isSetBlocked()) {
            shopSource.setBlockingUnblockedReason(null);
            shopSource.setBlockingUnblockedSince(null);
            shopSource.setBlockingBlockedReason(blocking.getBlocked().getReason());
            shopSource.setBlockingBlockedSince(TypeUtil.stringToLocalDateTime(blocking.getBlocked().getSince()));
        }
    }

}
