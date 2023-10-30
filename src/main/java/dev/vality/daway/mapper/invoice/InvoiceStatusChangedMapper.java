package dev.vality.daway.mapper.invoice;

import dev.vality.damsel.domain.InvoiceStatus;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.daway.domain.tables.pojos.InvoiceStatusInfo;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.mapper.Mapper;
import dev.vality.daway.model.InvoiceWrapper;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceStatusChangedMapper implements Mapper<InvoiceWrapper> {

    private Filter filter = new PathConditionFilter(
            new PathConditionRule("invoice_status_changed", new IsNullCondition().not()));

    @Override
    public InvoiceWrapper map(InvoiceChange invoiceChange, MachineEvent event, Integer changeId)
            throws DaoException {
        InvoiceStatus invoiceStatus = invoiceChange.getInvoiceStatusChanged().getStatus();
        long sequenceId = event.getEventId();
        String invoiceId = event.getSourceId();

        log.info("Start invoice status changed mapping, sequenceId={}, changeId={}, invoiceId={}, changeId={}, status={}",
                sequenceId, changeId, invoiceId, changeId, invoiceStatus.getSetField().getFieldName());

        InvoiceStatusInfo statusRecord = new InvoiceStatusInfo();
        statusRecord.setInvoiceId(invoiceId);
        statusRecord.setStatus(TBaseUtil.unionFieldToEnum(
                invoiceStatus,
                dev.vality.daway.domain.enums.InvoiceStatus.class
        ));
        if (invoiceStatus.isSetCancelled()) {
            statusRecord.setDetails(invoiceStatus.getCancelled().getDetails());
        } else if (invoiceStatus.isSetFulfilled()) {
            statusRecord.setDetails(invoiceStatus.getFulfilled().getDetails());
        }
        statusRecord.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        statusRecord.setChangeId(changeId);
        statusRecord.setSequenceId(sequenceId);

        log.info("Invoice has been mapped, sequenceId={}, changeId={}, invoiceId={}, changeId={}, status={}",
                sequenceId, changeId, invoiceId, changeId, invoiceStatus.getSetField().getFieldName());
        var invoiceWrapper = new InvoiceWrapper();
        invoiceWrapper.setInvoiceStatusInfo(statusRecord);
        return invoiceWrapper;
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
