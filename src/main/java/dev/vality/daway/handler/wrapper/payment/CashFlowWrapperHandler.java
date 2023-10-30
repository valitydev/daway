package dev.vality.daway.handler.wrapper.payment;

import dev.vality.daway.dao.invoicing.iface.CashFlowDao;
import dev.vality.daway.dao.invoicing.iface.CashFlowLinkDao;
import dev.vality.daway.dao.invoicing.impl.CashFlowLinkIdsGeneratorDaoImpl;
import dev.vality.daway.domain.tables.pojos.CashFlow;
import dev.vality.daway.domain.tables.pojos.CashFlowLink;
import dev.vality.daway.factory.invoice.payment.InvoicePaymentEventIdHolderFactory;
import dev.vality.daway.handler.wrapper.WrapperHandler;
import dev.vality.daway.model.CashFlowWrapper;
import dev.vality.daway.model.InvoicePaymentEventIdHolder;
import dev.vality.daway.model.InvoicingKey;
import dev.vality.daway.model.PaymentWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CashFlowWrapperHandler implements WrapperHandler<PaymentWrapper> {

    private final CashFlowLinkDao cashFlowLinkDao;

    private final CashFlowDao cashFlowDao;

    private final CashFlowLinkIdsGeneratorDaoImpl idsGenerator;

    @Override
    public boolean accept(List<PaymentWrapper> wrappers) {
        return wrappers.stream()
                .map(PaymentWrapper::getCashFlowWrapper)
                .anyMatch(Objects::nonNull);
    }

    @Override
    public void saveBatch(List<PaymentWrapper> wrappers) {
        List<CashFlowWrapper> cashFlowWrappers = wrappers.stream()
                .map(PaymentWrapper::getCashFlowWrapper)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        removeAlreadyProcessedWrappers(cashFlowWrappers);
        if (CollectionUtils.isEmpty(wrappers)) {
            return;
        }
        setLinkIds(cashFlowWrappers);
        saveCashFlowLinks(cashFlowWrappers);
        saveCashFlows(cashFlowWrappers);
    }

    private void removeAlreadyProcessedWrappers(List<CashFlowWrapper> cashFlowWrappers) {
        Set<InvoicePaymentEventIdHolder> existingEvents = cashFlowLinkDao.getExistingEvents(
                cashFlowWrappers.stream()
                        .map(CashFlowWrapper::getCashFlowLink)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
        cashFlowWrappers.removeIf(wrapper ->
                existingEvents.contains(InvoicePaymentEventIdHolderFactory.build(wrapper.getCashFlowLink()))
        );
    }

    private void setLinkIds(List<CashFlowWrapper> cashFlowWrappers) {
        Iterator<Long> linkIdIterator = idsGenerator.get(cashFlowWrappers.size()).iterator();
        for (CashFlowWrapper wrapper : cashFlowWrappers) {
            Long linkId = linkIdIterator.next();
            wrapper.getCashFlowLink().setId(linkId);
            for (CashFlow cashFlow : wrapper.getCashFlows()) {
                cashFlow.setObjId(linkId);
            }
        }
    }

    private void saveCashFlowLinks(List<CashFlowWrapper> cashFlowWrappers) {
        List<CashFlowLink> links = cashFlowWrappers.stream()
                .map(CashFlowWrapper::getCashFlowLink)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        cashFlowLinkDao.saveBatch(links);
        Set<InvoicingKey> invoicingKeys = links.stream()
                .map(link -> InvoicingKey.buildKey(link.getInvoiceId(), link.getPaymentId()))
                .collect(Collectors.toSet());
        cashFlowLinkDao.switchCurrent(invoicingKeys);
    }

    private void saveCashFlows(List<CashFlowWrapper> cashFlowWrappers) {
        List<CashFlow> cashFlows = cashFlowWrappers.stream()
                .flatMap(wrapper -> wrapper.getCashFlows().stream())
                .collect(Collectors.toList());
        cashFlowDao.save(cashFlows);
    }

}
