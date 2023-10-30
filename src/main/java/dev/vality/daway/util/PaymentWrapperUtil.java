package dev.vality.daway.util;

import dev.vality.daway.model.InvoicingKey;
import dev.vality.daway.model.PaymentWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentWrapperUtil {

    public static Set<InvoicingKey> getInvoicingKeys(List<PaymentWrapper> paymentWrappers) {
        return paymentWrappers.stream()
                .map(PaymentWrapper::getKey)
                .collect(Collectors.toSet());
    }

}
