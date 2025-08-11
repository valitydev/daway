package dev.vality.daway.mapper.invoice;

import com.fasterxml.jackson.databind.JsonNode;
import dev.vality.damsel.domain.Invoice;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.daway.domain.enums.InvoiceStatus;
import dev.vality.daway.domain.tables.pojos.InvoiceCart;
import dev.vality.daway.domain.tables.pojos.InvoiceStatusInfo;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.mapper.Mapper;
import dev.vality.daway.model.InvoiceWrapper;
import dev.vality.daway.util.JsonUtil;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceCreatedMapper implements Mapper<InvoiceWrapper> {

    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("invoice_created", new IsNullCondition().not())
    );

    @Override
    public InvoiceWrapper map(InvoiceChange invoiceChange, MachineEvent event, Integer changeId)
            throws DaoException {
        Invoice invoice = invoiceChange.getInvoiceCreated().getInvoice();
        long sequenceId = event.getEventId();
        String invoiceId = event.getSourceId();

        log.info("Start invoice created mapping, sequenceId={}, changeId={}, invoiceId={}, partyId={}, shopId={}",
                sequenceId, changeId, invoiceId, invoice.getOwnerId(), invoice.getShopId());

        InvoiceWrapper invoiceWrapper = new InvoiceWrapper();
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(event.getCreatedAt());
        invoiceWrapper.setInvoice(getInvoice(invoice, sequenceId, changeId, eventCreatedAt));
        invoiceWrapper.setInvoiceStatusInfo(getInvoiceStatusInfo(invoice, sequenceId, changeId, eventCreatedAt));
        if (invoice.getDetails().isSetCart()) {
            invoiceWrapper.setCarts(getInvoiceCarts(invoice, sequenceId, changeId, eventCreatedAt));
        }
        log.info("Invoice has been mapped, sequenceId={}, changeId={}, invoiceId={}, partyId={}, shopId={}",
                sequenceId, changeId, invoiceId, invoice.getOwnerId(), invoice.getShopId());
        return invoiceWrapper;
    }

    private dev.vality.daway.domain.tables.pojos.Invoice getInvoice(Invoice invoice,
                                                                    Long sequenceId,
                                                                    Integer changeId,
                                                                    LocalDateTime eventCreatedAt) {
        var invoiceRecord = new dev.vality.daway.domain.tables.pojos.Invoice();
        invoiceRecord.setChangeId(changeId);
        invoiceRecord.setSequenceId(sequenceId);
        invoiceRecord.setEventCreatedAt(eventCreatedAt);
        invoiceRecord.setInvoiceId(invoice.getId());
        invoiceRecord.setExternalId(invoice.getExternalId());
        invoiceRecord.setPartyId(invoice.getOwnerId());
        invoiceRecord.setShopId(invoice.getShopId());
        invoiceRecord.setPartyRevision(invoice.getDomainRevision());
        invoiceRecord.setCreatedAt(TypeUtil.stringToLocalDateTime(invoice.getCreatedAt()));
        invoiceRecord.setDetailsProduct(invoice.getDetails().getProduct());
        invoiceRecord.setDetailsDescription(invoice.getDetails().getDescription());
        invoiceRecord.setDue(TypeUtil.stringToLocalDateTime(invoice.getDue()));
        invoiceRecord.setAmount(invoice.getCost().getAmount());
        invoiceRecord.setCurrencyCode(invoice.getCost().getCurrency().getSymbolicCode());
        invoiceRecord.setContext(invoice.getContext().getData());
        invoiceRecord.setTemplateId(invoice.getTemplateId());

        return invoiceRecord;
    }

    private InvoiceStatusInfo getInvoiceStatusInfo(Invoice invoice,
                                                   Long sequenceId,
                                                   Integer changeId,
                                                   LocalDateTime eventCreatedAt) {
        InvoiceStatusInfo statusRecord = new InvoiceStatusInfo();
        statusRecord.setInvoiceId(invoice.getId());
        statusRecord.setStatus(TBaseUtil.unionFieldToEnum(invoice.getStatus(), InvoiceStatus.class));
        if (invoice.getStatus().isSetCancelled()) {
            statusRecord.setDetails(invoice.getStatus().getCancelled().getDetails());
        } else if (invoice.getStatus().isSetFulfilled()) {
            statusRecord.setDetails(invoice.getStatus().getFulfilled().getDetails());
        }
        statusRecord.setEventCreatedAt(eventCreatedAt);
        statusRecord.setChangeId(changeId);
        statusRecord.setSequenceId(sequenceId);
        statusRecord.setExternalId(invoice.getExternalId());

        return statusRecord;
    }

    private List<InvoiceCart> getInvoiceCarts(Invoice invoice,
                                              Long sequenceId,
                                              Integer changeId,
                                              LocalDateTime eventCreatedAt) {
        return invoice.getDetails().getCart().getLines().stream().map(il -> {
                    InvoiceCart ic = new InvoiceCart();
                    ic.setEventCreatedAt(eventCreatedAt);
                    ic.setInvoiceId(invoice.getId());
                    ic.setProduct(il.getProduct());
                    ic.setQuantity(il.getQuantity());
                    ic.setAmount(il.getPrice().getAmount());
                    ic.setCurrencyCode(il.getPrice().getCurrency().getSymbolicCode());
                    Map<String, JsonNode> jsonNodeMap = il.getMetadata().entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> JsonUtil.thriftBaseToJsonNode(e.getValue())));
                    ic.setMetadataJson(JsonUtil.objectToJsonString(jsonNodeMap));
                    ic.setSequenceId(sequenceId);
                    ic.setChangeId(changeId);
                    return ic;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
