package dev.vality.daway.handler.event.stock.impl.partymngmnt.shop;

import dev.vality.damsel.domain.Suspension;
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
@SuppressWarnings("VariableDeclarationUsageDistance")
public class ShopSuspensionHandler implements PartyManagementHandler {

    private final ShopDao shopDao;
    private final ClaimEffectCopyFactory<Shop, Integer> claimEffectCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(new PathConditionRule("shop_suspension",
            new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        Suspension suspension = change.getShopSuspension().getSuspension();
        String shopId = change.getShopSuspension().getShopId();
        String partyId = event.getSourceId();
        log.info("Start shop suspension handling, sequenceId={}, partyId={}, shopId={}, changeId={}",
                sequenceId, partyId, shopId, changeId);

        Shop shopOld = shopDao.get(partyId, shopId);
        Shop shopNew = claimEffectCopyFactory.create(event, sequenceId, -1, changeId, shopOld);

        shopNew.setSuspension(
                TBaseUtil.unionFieldToEnum(suspension, dev.vality.daway.domain.enums.Suspension.class));
        if (suspension.isSetActive()) {
            shopNew.setSuspensionActiveSince(TypeUtil.stringToLocalDateTime(suspension.getActive().getSince()));
            shopNew.setSuspensionSuspendedSince(null);
        } else if (suspension.isSetSuspended()) {
            shopNew.setSuspensionActiveSince(null);
            shopNew.setSuspensionSuspendedSince(TypeUtil.stringToLocalDateTime(suspension.getSuspended().getSince()));
        }

        shopDao.saveWithUpdateCurrent(shopNew, shopOld.getId(), "suspension");
    }

}
