package dev.vality.daway.handler.wrapper.payment;

import dev.vality.daway.dao.invoicing.iface.PaymentRecurrentInfoDao;
import dev.vality.daway.domain.tables.pojos.PaymentRecurrentInfo;
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
public class PaymentRecurrentInfoWrapperHandler implements WrapperHandler<PaymentWrapper> {

    private final PaymentRecurrentInfoDao paymentRecurrentInfoDao;

    @Override
    public boolean accept(List<PaymentWrapper> wrappers) {
        return wrappers.stream()
                .map(PaymentWrapper::getPaymentRecurrentInfo)
                .anyMatch(Objects::nonNull);
    }

    @Override
    public void saveBatch(List<PaymentWrapper> wrappers) {
        List<PaymentWrapper> processableWrappers = wrappers.stream()
                .filter(paymentWrapper -> Objects.nonNull(paymentWrapper.getPaymentRecurrentInfo()))
                .collect(Collectors.toList());
        List<PaymentRecurrentInfo> paymentRecurrentInfos = processableWrappers.stream()
                .map(PaymentWrapper::getPaymentRecurrentInfo)
                .collect(Collectors.toList());
        paymentRecurrentInfoDao.saveBatch(paymentRecurrentInfos);
        paymentRecurrentInfoDao.switchCurrent(PaymentWrapperUtil.getInvoicingKeys(processableWrappers));
    }
}
