package dev.vality.daway.handler.wrapper.payment;

import dev.vality.daway.dao.invoicing.iface.PaymentSessionInfoDao;
import dev.vality.daway.domain.tables.pojos.PaymentSessionInfo;
import dev.vality.daway.handler.wrapper.WrapperHandler;
import dev.vality.daway.model.PaymentWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class PaymentSessionWrapperHandler implements WrapperHandler<PaymentWrapper> {

    private final PaymentSessionInfoDao paymentSessionInfoDao;

    @Override
    public boolean accept(List<PaymentWrapper> wrappers) {
        return wrappers.stream()
                .map(PaymentWrapper::getPaymentSessionInfo)
                .anyMatch(Objects::nonNull);
    }

    @Override
    public void saveBatch(List<PaymentWrapper> wrappers) {
        List<PaymentSessionInfo> payments = wrappers.stream()
                .map(PaymentWrapper::getPaymentSessionInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        paymentSessionInfoDao.saveBatch(payments);
    }
}
