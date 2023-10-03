package dev.vality.newway.mapper.payment;

import dev.vality.damsel.domain.AdditionalTransactionInfo;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.InvoicePaymentSessionChange;
import dev.vality.damsel.payment_processing.SessionChangePayload;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.newway.dao.invoicing.iface.PaymentAdditionalInfoDao;
import dev.vality.newway.domain.tables.pojos.PaymentAdditionalInfo;
import dev.vality.newway.mapper.Mapper;
import dev.vality.newway.model.InvoicingKey;
import dev.vality.newway.model.PaymentWrapper;
import dev.vality.newway.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentSessionChangeTransactionBoundMapper implements Mapper<PaymentWrapper> {

    private final PaymentAdditionalInfoDao paymentAdditionalInfoDao;

    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_session_change.payload.session_transaction_bound",
            new IsNullCondition().not()));

    @Override
    public PaymentWrapper map(InvoiceChange change, MachineEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePaymentChange.getId();
        long sequenceId = event.getEventId();
        log.info(
                "Start mapping session change transaction info, sequenceId='{}', changeId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, changeId, invoiceId, paymentId);
        InvoicePaymentSessionChange sessionChange = invoicePaymentChange.getPayload().getInvoicePaymentSessionChange();
        SessionChangePayload payload = sessionChange.getPayload();
        TransactionInfo transactionInfo = payload.getSessionTransactionBound().getTrx();
        PaymentAdditionalInfo paymentAdditionalInfo = paymentAdditionalInfoDao.safeGet(invoiceId, paymentId);
        PaymentAdditionalInfo additionalInfo = new PaymentAdditionalInfo();
        if (paymentAdditionalInfo != null) {
            additionalInfo = paymentAdditionalInfo;
        }
        additionalInfo.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        additionalInfo.setInvoiceId(invoiceId);
        additionalInfo.setPaymentId(paymentId);
        additionalInfo.setTransactionId(transactionInfo.getId());
        Map<String, String> extra = transactionInfo.getExtra();
        extra.replace("PaRes", null);
        additionalInfo.setExtraJson(JsonUtil.objectToJsonString(extra));
        if (transactionInfo.isSetAdditionalInfo()) {
            AdditionalTransactionInfo additionalInfoSource = transactionInfo.getAdditionalInfo();
            additionalInfo.setRrn(additionalInfoSource.getRrn());
            additionalInfo.setApprovalCode(additionalInfoSource.getApprovalCode());
            additionalInfo.setAcsUrl(additionalInfoSource.getAcsUrl());
            additionalInfo.setMd(additionalInfoSource.getMd());
            additionalInfo.setTermUrl(additionalInfoSource.getTermUrl());
            additionalInfo.setEci(additionalInfoSource.getEci());
            additionalInfo.setCavv(additionalInfoSource.getCavv());
            additionalInfo.setXid(additionalInfoSource.getXid());
            additionalInfo.setCavvAlgorithm(additionalInfoSource.getCavvAlgorithm());
            if (additionalInfoSource.isSetThreeDsVerification()) {
                additionalInfo.setThreeDsVerification(additionalInfoSource.getThreeDsVerification().name());
            }
        }
        additionalInfo.setSequenceId(sequenceId);
        additionalInfo.setChangeId(changeId);
        log.info(
                "Payment session transaction info has been mapped, sequenceId='{}', changeId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, changeId, invoiceId, paymentId);
        PaymentWrapper paymentWrapper = new PaymentWrapper();
        paymentWrapper.setKey(InvoicingKey.buildKey(invoiceId, paymentId));
        paymentWrapper.setPaymentAdditionalInfo(additionalInfo);
        return paymentWrapper;
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
