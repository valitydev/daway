package dev.vality.daway.handler.wrapper.invoice;

import dev.vality.daway.dao.invoicing.iface.InvoiceCartDao;
import dev.vality.daway.domain.tables.pojos.InvoiceCart;
import dev.vality.daway.handler.wrapper.WrapperHandler;
import dev.vality.daway.model.InvoiceWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class InvoiceCartWrapperHandler implements WrapperHandler<InvoiceWrapper> {

    private final InvoiceCartDao invoiceCartDao;

    @Override
    public boolean accept(List<InvoiceWrapper> wrappers) {
        return wrappers.stream()
                .map(InvoiceWrapper::getCarts)
                .anyMatch(invoiceCarts -> !CollectionUtils.isEmpty(invoiceCarts));
    }

    @Override
    public void saveBatch(List<InvoiceWrapper> wrappers) {
        List<InvoiceCart> carts = wrappers.stream()
                .map(InvoiceWrapper::getCarts)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Set<String> invoiceIds = carts.stream()
                .map(InvoiceCart::getInvoiceId)
                .collect(Collectors.toSet());
        Set<String> existingInvoiceIds = invoiceCartDao.getExistingInvoiceIds(invoiceIds);
        carts.removeIf(cart -> existingInvoiceIds.contains(cart.getInvoiceId()));
        if (!carts.isEmpty()) {
            invoiceCartDao.save(carts);
        }
    }

}
