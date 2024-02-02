package dev.vality.daway.handler.wrapper.payment;

import dev.vality.daway.dao.invoicing.iface.PaymentCashChangeDao;
import dev.vality.daway.domain.tables.pojos.PaymentCashChange;
import dev.vality.daway.handler.wrapper.WrapperHandler;
import dev.vality.daway.model.PaymentWrapper;
import dev.vality.daway.util.PaymentWrapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class PaymentCashChangeWrapperHandler implements WrapperHandler<PaymentWrapper> {

    private final PaymentCashChangeDao paymentCashChangeDao;

    @Override
    public boolean accept(List<PaymentWrapper> wrappers) {
        return wrappers.stream()
                .map(PaymentWrapper::getPaymentCashChange)
                .anyMatch(Objects::nonNull);
    }

    @Override
    public void saveBatch(List<PaymentWrapper> wrappers) {
        List<PaymentWrapper> processableWrappers = wrappers.stream()
                .filter(paymentWrapper -> Objects.nonNull(paymentWrapper.getPaymentCashChange()))
                .collect(Collectors.toList());
        List<PaymentCashChange> paymentCashChanges = processableWrappers.stream()
                .map(PaymentWrapper::getPaymentCashChange)
                .collect(Collectors.toList());
        paymentCashChangeDao.saveBatch(paymentCashChanges);
        paymentCashChangeDao.switchCurrent(PaymentWrapperUtil.getInvoicingKeys(processableWrappers));
    }
}
