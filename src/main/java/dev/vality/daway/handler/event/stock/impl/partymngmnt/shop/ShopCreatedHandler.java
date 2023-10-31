package dev.vality.daway.handler.event.stock.impl.partymngmnt.shop;

import dev.vality.damsel.domain.Shop;
import dev.vality.damsel.payment_processing.ClaimEffect;
import dev.vality.damsel.payment_processing.PartyChange;
import dev.vality.damsel.payment_processing.ShopEffectUnit;
import dev.vality.daway.dao.party.iface.PartyDao;
import dev.vality.daway.dao.party.iface.ShopDao;
import dev.vality.daway.domain.enums.Blocking;
import dev.vality.daway.domain.enums.Suspension;
import dev.vality.daway.handler.event.stock.impl.partymngmnt.AbstractClaimChangedHandler;
import dev.vality.daway.util.ShopUtil;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Slf4j
@Component
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ShopCreatedHandler extends AbstractClaimChangedHandler {

    private final ShopDao shopDao;
    private final PartyDao partyDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, MachineEvent event, Integer changeId) {
        List<ClaimEffect> claimEffects = getClaimStatus(change).getAccepted().getEffects();
        for (int i = 0; i < claimEffects.size(); i++) {
            ClaimEffect claimEffect = claimEffects.get(i);
            if (claimEffect.isSetShopEffect() && claimEffect.getShopEffect().getEffect().isSetCreated()) {
                handleEvent(event, changeId, claimEffects.get(i), i);
            }
        }
    }

    private void handleEvent(MachineEvent event, Integer changeId, ClaimEffect e, Integer claimEffectId) {
        long sequenceId = event.getEventId();
        ShopEffectUnit shopEffect = e.getShopEffect();
        Shop shopCreated = shopEffect.getEffect().getCreated();
        String shopId = shopEffect.getShopId();
        String partyId = event.getSourceId();
        log.info("Start shop created handling, sequenceId={}, partyId={}, shopId={}, changeId={}",
                sequenceId, partyId, shopId, changeId);

        partyDao.get(partyId); //check party is exist
        dev.vality.daway.domain.tables.pojos.Shop shop =
                createShop(event, changeId, sequenceId, shopCreated, shopId, partyId, claimEffectId);

        shopDao.save(shop).ifPresentOrElse(
                atLong -> log.info("Shop has been saved, sequenceId={}, ppartyId={}, shopId={}, changeId={}",
                        sequenceId,
                        partyId, shopId, changeId),
                () -> log.info("Shop create duplicated, sequenceId={}, partyId={}, shopId={}, changeId={}", sequenceId,
                        partyId, shopId, changeId));
    }

    private dev.vality.daway.domain.tables.pojos.Shop createShop(MachineEvent event, Integer changeId,
                                                                 long sequenceId,
                                                                 Shop shopCreated, String shopId, String partyId,
                                                                 Integer claimEffectId) {
        dev.vality.daway.domain.tables.pojos.Shop shop = new dev.vality.daway.domain.tables.pojos.Shop();
        shop.setSequenceId((int) sequenceId);
        shop.setChangeId(changeId);
        shop.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        shop.setShopId(shopId);
        shop.setPartyId(partyId);
        shop.setClaimEffectId(claimEffectId);
        shop.setCreatedAt(TypeUtil.stringToLocalDateTime(shopCreated.getCreatedAt()));
        shop.setBlocking(
                TBaseUtil.unionFieldToEnum(shopCreated.getBlocking(), Blocking.class));
        if (shopCreated.getBlocking().isSetUnblocked()) {
            shop.setBlockingUnblockedReason(shopCreated.getBlocking().getUnblocked().getReason());
            shop.setBlockingUnblockedSince(
                    TypeUtil.stringToLocalDateTime(shopCreated.getBlocking().getUnblocked().getSince()));
        } else if (shopCreated.getBlocking().isSetBlocked()) {
            shop.setBlockingBlockedReason(shopCreated.getBlocking().getBlocked().getReason());
            shop.setBlockingBlockedSince(
                    TypeUtil.stringToLocalDateTime(shopCreated.getBlocking().getBlocked().getSince()));
        }
        shop.setSuspension(TBaseUtil
                .unionFieldToEnum(shopCreated.getSuspension(), Suspension.class));
        if (shopCreated.getSuspension().isSetActive()) {
            shop.setSuspensionActiveSince(
                    TypeUtil.stringToLocalDateTime(shopCreated.getSuspension().getActive().getSince()));
        } else if (shopCreated.getSuspension().isSetSuspended()) {
            shop.setSuspensionSuspendedSince(
                    TypeUtil.stringToLocalDateTime(shopCreated.getSuspension().getSuspended().getSince()));
        }
        shop.setDetailsName(shopCreated.getDetails().getName());
        shop.setDetailsDescription(shopCreated.getDetails().getDescription());
        if (shopCreated.getLocation().isSetUrl()) {
            shop.setLocationUrl(shopCreated.getLocation().getUrl());
        } else {
            throw new IllegalArgumentException("Illegal shop location " + shopCreated.getLocation());
        }
        shop.setCategoryId(shopCreated.getCategory().getId());
        if (shopCreated.isSetAccount()) {
            ShopUtil.fillShopAccount(shop, shopCreated.getAccount());
        }
        shop.setContractId(shopCreated.getContractId());
        shop.setPayoutToolId(shopCreated.getPayoutToolId());
        if (shopCreated.isSetPayoutSchedule()) {
            shop.setPayoutScheduleId(shopCreated.getPayoutSchedule().getId());
        }
        return shop;
    }
}
