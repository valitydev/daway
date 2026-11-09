package dev.vality.daway.service;

import com.github.benmanes.caffeine.cache.Cache;
import dev.vality.daway.dao.invoicing.iface.InvoiceDao;
import dev.vality.daway.domain.tables.pojos.Invoice;
import dev.vality.daway.model.InvoiceWrapper;
import dev.vality.daway.model.PartyShop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PartyShopCacheService {

    private final InvoiceDao invoiceDao;
    private final Cache<String, PartyShop> partyShopDataCache;

    public void put(InvoiceWrapper invoiceWrapper) {
        if (invoiceWrapper.getInvoice() == null) {
            return;
        }
        partyShopDataCache.put(
                invoiceWrapper.getInvoice().getInvoiceId(),
                new PartyShop(
                        invoiceWrapper.getInvoice().getPartyId(),
                        invoiceWrapper.getInvoice().getShopId()
                )
        );
    }

    public PartyShop get(String invoiceId) {
        PartyShop partyShop = partyShopDataCache.getIfPresent(invoiceId);
        if (partyShop != null) {
            return new PartyShop(partyShop.getPartyId(), partyShop.getShopId());
        }
        log.info("Cache miss for invoiceId: {}", invoiceId);
        Invoice invoice = invoiceDao.get(invoiceId);
        if (invoice == null) {
            log.warn("Invoice was not found for invoiceId: {}", invoiceId);
            return null;
        }
        partyShop = new PartyShop(invoice.getPartyId(), invoice.getShopId());
        partyShopDataCache.put(invoiceId, partyShop);
        return partyShop;
    }

}
