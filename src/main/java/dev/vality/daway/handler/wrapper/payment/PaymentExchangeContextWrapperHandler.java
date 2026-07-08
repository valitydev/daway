package dev.vality.daway.handler.wrapper.payment;

import dev.vality.daway.dao.invoicing.iface.PaymentExchangeContextDao;
import dev.vality.daway.domain.tables.pojos.PaymentExchangeContext;
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
public class PaymentExchangeContextWrapperHandler implements WrapperHandler<PaymentWrapper> {

    private final PaymentExchangeContextDao paymentExchangeContextDao;

    @Override
    public boolean accept(List<PaymentWrapper> wrappers) {
        return wrappers.stream()
                .map(PaymentWrapper::getPaymentExchangeContext)
                .anyMatch(Objects::nonNull);
    }

    @Override
    public void saveBatch(List<PaymentWrapper> wrappers) {
        List<PaymentWrapper> processableWrappers = wrappers.stream()
                .filter(paymentWrapper -> Objects.nonNull(paymentWrapper.getPaymentExchangeContext()))
                .collect(Collectors.toList());
        List<PaymentExchangeContext> paymentExchangeContexts = processableWrappers.stream()
                .map(PaymentWrapper::getPaymentExchangeContext)
                .collect(Collectors.toList());
        paymentExchangeContextDao.saveBatch(paymentExchangeContexts);
        paymentExchangeContextDao.switchCurrent(PaymentWrapperUtil.getInvoicingKeys(processableWrappers));
    }
}
