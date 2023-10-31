package dev.vality.daway.handler.wrapper.payment;

import dev.vality.daway.dao.invoicing.iface.PaymentRouteDao;
import dev.vality.daway.domain.tables.pojos.PaymentRoute;
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
public class PaymentRouteWrapperHandler implements WrapperHandler<PaymentWrapper> {

    private final PaymentRouteDao paymentRouteDao;

    @Override
    public boolean accept(List<PaymentWrapper> wrappers) {
        return wrappers.stream()
                .map(PaymentWrapper::getPaymentRoute)
                .anyMatch(Objects::nonNull);
    }

    @Override
    public void saveBatch(List<PaymentWrapper> wrappers) {
        List<PaymentWrapper> processableWrappers = wrappers.stream()
                .filter(paymentWrapper -> Objects.nonNull(paymentWrapper.getPaymentRoute()))
                .collect(Collectors.toList());
        List<PaymentRoute> paymentRoutes = processableWrappers.stream()
                .map(PaymentWrapper::getPaymentRoute)
                .collect(Collectors.toList());
        paymentRouteDao.saveBatch(paymentRoutes);
        paymentRouteDao.switchCurrent(PaymentWrapperUtil.getInvoicingKeys(processableWrappers));
    }
}
