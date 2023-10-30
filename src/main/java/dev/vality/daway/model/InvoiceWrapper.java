package dev.vality.daway.model;

import dev.vality.daway.domain.tables.pojos.Invoice;
import dev.vality.daway.domain.tables.pojos.InvoiceCart;
import dev.vality.daway.domain.tables.pojos.InvoiceStatusInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceWrapper {
    private Invoice invoice;
    private InvoiceStatusInfo invoiceStatusInfo;
    private List<InvoiceCart> carts;
}
