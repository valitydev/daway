package dev.vality.daway.handler.wrapper.invoice;

import dev.vality.daway.dao.invoicing.iface.InvoiceDao;
import dev.vality.daway.domain.tables.pojos.Invoice;
import dev.vality.daway.handler.wrapper.WrapperHandler;
import dev.vality.daway.model.InvoiceWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class InvoiceWrapperHandler implements WrapperHandler<InvoiceWrapper> {

    private final InvoiceDao invoiceDao;

    @Override
    public boolean accept(List<InvoiceWrapper> wrappers) {
        return wrappers.stream()
                .map(InvoiceWrapper::getInvoice)
                .anyMatch(Objects::nonNull);
    }

    @Override
    public void saveBatch(List<InvoiceWrapper> wrappers) {
        List<Invoice> invoices = wrappers.stream()
                .map(InvoiceWrapper::getInvoice)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        invoiceDao.saveBatch(invoices);
    }

}
