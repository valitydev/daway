package dev.vality.daway.handler.wrapper.payment;

import dev.vality.daway.dao.invoicing.iface.PaymentFeeDao;
import dev.vality.daway.domain.tables.pojos.PaymentFee;
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
public class PaymentFeeWrapperHandler implements WrapperHandler<PaymentWrapper> {

    private final PaymentFeeDao paymentFeeDao;

    @Override
    public boolean accept(List<PaymentWrapper> wrappers) {
        return wrappers.stream()
                .map(PaymentWrapper::getPaymentFee)
                .anyMatch(Objects::nonNull);
    }

    @Override
    public void saveBatch(List<PaymentWrapper> wrappers) {
        List<PaymentWrapper> processableWrappers = wrappers.stream()
                .filter(paymentWrapper -> Objects.nonNull(paymentWrapper.getPaymentFee()))
                .collect(Collectors.toList());
        List<PaymentFee> paymentFees = processableWrappers.stream()
                .map(PaymentWrapper::getPaymentFee)
                .collect(Collectors.toList());
        paymentFeeDao.saveBatch(paymentFees);
        paymentFeeDao.switchCurrent(PaymentWrapperUtil.getInvoicingKeys(processableWrappers));
    }
}
