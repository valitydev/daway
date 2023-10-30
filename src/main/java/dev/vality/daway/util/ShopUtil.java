package dev.vality.daway.util;

import dev.vality.damsel.domain.ShopAccount;
import dev.vality.daway.domain.tables.pojos.Shop;

public class ShopUtil {

    public static void fillShopAccount(Shop shop, ShopAccount shopAccount) {
        shop.setAccountCurrencyCode(shopAccount.getCurrency().getSymbolicCode());
        shop.setAccountGuarantee(shopAccount.getGuarantee());
        shop.setAccountSettlement(shopAccount.getSettlement());
        shop.setAccountPayout(shopAccount.getPayout());
    }

}
