package dev.vality.daway.handler.wrapper.payment;

import dev.vality.daway.dao.invoicing.iface.PaymentAdditionalInfoDao;
import dev.vality.daway.domain.tables.pojos.PaymentAdditionalInfo;
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
public class PaymentAdditionalInfoWrapperHandler implements WrapperHandler<PaymentWrapper> {

    private final PaymentAdditionalInfoDao paymentAdditionalInfoDao;

    @Override
    public boolean accept(List<PaymentWrapper> wrappers) {
        return wrappers.stream()
                .map(PaymentWrapper::getPaymentAdditionalInfo)
                .anyMatch(Objects::nonNull);
    }

    @Override
    public void saveBatch(List<PaymentWrapper> wrappers) {
        List<PaymentWrapper> processableWrappers = wrappers.stream()
                .filter(paymentWrapper -> Objects.nonNull(paymentWrapper.getPaymentAdditionalInfo()))
                .collect(Collectors.toList());
        List<PaymentAdditionalInfo> paymentAdditionalInfos = processableWrappers.stream()
                .map(PaymentWrapper::getPaymentAdditionalInfo)
                .collect(Collectors.toList());
        paymentAdditionalInfoDao.saveBatch(paymentAdditionalInfos);
        paymentAdditionalInfoDao.switchCurrent(PaymentWrapperUtil.getInvoicingKeys(processableWrappers));
    }
}
