package dev.vality.daway.handler.wrapper.invoice;

import dev.vality.daway.dao.invoicing.iface.InvoiceStatusInfoDao;
import dev.vality.daway.domain.tables.pojos.InvoiceStatusInfo;
import dev.vality.daway.handler.wrapper.WrapperHandler;
import dev.vality.daway.model.InvoiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class InvoiceStatusInfoWrapperHandler implements WrapperHandler<InvoiceWrapper> {

    private final InvoiceStatusInfoDao invoiceStatusInfoDao;

    @Override
    public boolean accept(List<InvoiceWrapper> wrappers) {
        return wrappers.stream()
                .map(InvoiceWrapper::getInvoiceStatusInfo)
                .anyMatch(Objects::nonNull);
    }

    @Override
    public void saveBatch(List<InvoiceWrapper> wrappers) {
        saveInvoiceStatusInfos(wrappers);
        switchCurrent(wrappers);
    }

    private void saveInvoiceStatusInfos(List<InvoiceWrapper> wrappers) {
        List<InvoiceStatusInfo> invoiceStatusInfos = wrappers.stream()
                .map(InvoiceWrapper::getInvoiceStatusInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        invoiceStatusInfoDao.saveBatch(invoiceStatusInfos);
    }

    private void switchCurrent(List<InvoiceWrapper> wrappers) {
        Set<String> invoiceStatusInfoChangedIds = wrappers.stream()
                .map(InvoiceWrapper::getInvoiceStatusInfo)
                .filter(Objects::nonNull)
                .map(InvoiceStatusInfo::getInvoiceId)
                .collect(Collectors.toSet());
        log.info("Switch to current ids - invoiceStatusInfo:{}", invoiceStatusInfoChangedIds);
        invoiceStatusInfoDao.switchCurrent(invoiceStatusInfoChangedIds);
    }
}
