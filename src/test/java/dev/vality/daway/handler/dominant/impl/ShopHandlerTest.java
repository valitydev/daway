package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.Reference;
import dev.vality.damsel.domain.ShopConfigRef;
import dev.vality.damsel.domain_config_v2.FinalOperation;
import dev.vality.damsel.domain_config_v2.RemoveOp;
import dev.vality.daway.dao.dominant.impl.ShopDaoImpl;
import dev.vality.daway.domain.tables.pojos.Shop;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShopHandlerTest {

    @Mock
    private ShopDaoImpl shopDao;

    @Test
    void handleRemoveOperationByShopConfigReference() {
        String shopId = "561b311f-803c-4caf-a8d0-8241f298a727";
        ShopHandler shopHandler = new ShopHandler(shopDao);
        FinalOperation operation = FinalOperation.remove(new RemoveOp()
                .setRef(Reference.shop_config(new ShopConfigRef(shopId))));

        assertTrue(shopHandler.acceptAndSet(operation));
        shopHandler.handle(operation, 6079L, "2026-06-05T08:54:58Z");

        verify(shopDao).updateNotCurrent(shopId);
        verify(shopDao, never()).save(org.mockito.ArgumentMatchers.any(Shop.class));
    }
}
