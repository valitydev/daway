package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.ShopConfig;
import dev.vality.damsel.domain.ShopConfigObject;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.party.impl.ShopDaoImpl;
import dev.vality.daway.domain.enums.Blocking;
import dev.vality.daway.domain.enums.Suspension;
import dev.vality.daway.domain.tables.pojos.Shop;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ShopHandler extends AbstractDominantHandler<ShopConfigObject, Shop, String> {

    private final ShopDaoImpl shopDao;

    public ShopHandler(ShopDaoImpl shopDao) {
        this.shopDao = shopDao;
    }

    @Override
    protected DomainObjectDao<Shop, String> getDomainObjectDao() {
        return shopDao;
    }

    @Override
    protected ShopConfigObject getTargetObject() {
        return getDomainObject().getShopConfig();
    }

    @Override
    protected String getTargetObjectRefId() {
        return getTargetObject().getRef().getId();
    }

    @Override
    protected String getTargetRefId() {
        return getReference().getShopConfig().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetShopConfig();
    }

    @Override
    public Shop convertToDatabaseObject(ShopConfigObject shopConfigObject, Long versionId, boolean current,
                                        String createdAt) {
        dev.vality.daway.domain.tables.pojos.Shop shop = new dev.vality.daway.domain.tables.pojos.Shop();
        ShopConfig data = shopConfigObject.getData();
        LocalDateTime createAt = TypeUtil.stringToLocalDateTime(createdAt);
        shop.setWtime(createAt);
        shop.setCreatedAt(createAt);
        shop.setEventCreatedAt(createAt);
        shop.setShopId(shopConfigObject.getRef().id);
        shop.setPartyId(data.getPartyRef().id);
        shop.setBlocking(TBaseUtil.unionFieldToEnum(data.getBlock(), Blocking.class));
        if (data.getBlock().isSetUnblocked()) {
            shop.setBlockingUnblockedReason(data.getBlock().getUnblocked().getReason());
            shop.setBlockingUnblockedSince(
                    TypeUtil.stringToLocalDateTime(data.getBlock().getUnblocked().getSince()));
        } else if (data.getBlock().isSetBlocked()) {
            shop.setBlockingBlockedReason(data.getBlock().getBlocked().getReason());
            shop.setBlockingBlockedSince(
                    TypeUtil.stringToLocalDateTime(data.getBlock().getBlocked().getSince()));
        }
        shop.setSuspension(TBaseUtil
                .unionFieldToEnum(data.getSuspension(), Suspension.class));
        if (data.getSuspension().isSetActive()) {
            shop.setSuspensionActiveSince(
                    TypeUtil.stringToLocalDateTime(data.getSuspension().getActive().getSince()));
        } else if (data.getSuspension().isSetSuspended()) {
            shop.setSuspensionSuspendedSince(
                    TypeUtil.stringToLocalDateTime(data.getSuspension().getSuspended().getSince()));
        }
        shop.setDetailsDescription(data.getDescription());
        if (data.getLocation().isSetUrl()) {
            shop.setLocationUrl(data.getLocation().getUrl());
        } else {
            throw new IllegalArgumentException("Illegal shop location " + data.getLocation());
        }
        shop.setCategoryId(data.getCategory().getId());
        return shop;
    }
}
