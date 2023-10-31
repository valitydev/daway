package dev.vality.daway.mapper.payment;

import dev.vality.damsel.domain.RiskScore;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.daway.domain.tables.pojos.PaymentRiskData;
import dev.vality.daway.mapper.Mapper;
import dev.vality.daway.model.InvoicingKey;
import dev.vality.daway.model.PaymentWrapper;
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
public class InvoicePaymentRiskScoreChangedMapper implements Mapper<PaymentWrapper> {

    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_risk_score_changed",
            new IsNullCondition().not()));

    @Override
    public PaymentWrapper map(InvoiceChange change, MachineEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePaymentChange.getId();
        long sequenceId = event.getEventId();
        log.info("Start mapping payment risk score change, sequenceId='{}', changeId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, changeId, invoiceId, paymentId);
        RiskScore riskScore = invoicePaymentChange.getPayload().getInvoicePaymentRiskScoreChanged().getRiskScore();
        dev.vality.daway.domain.enums.RiskScore score =
                TypeUtil.toEnumField(riskScore.name(), dev.vality.daway.domain.enums.RiskScore.class);
        if (score == null) {
            throw new IllegalArgumentException("Illegal risk score: " + riskScore);
        }
        PaymentRiskData paymentRiskData = new PaymentRiskData();
        paymentRiskData.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        paymentRiskData.setInvoiceId(invoiceId);
        paymentRiskData.setPaymentId(paymentId);
        paymentRiskData.setRiskScore(score);
        paymentRiskData.setSequenceId(sequenceId);
        paymentRiskData.setChangeId(changeId);
        log.info("Payment risk score have been mapped, sequenceId='{}', changeId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, changeId, invoiceId, paymentId);
        PaymentWrapper paymentWrapper = new PaymentWrapper();
        paymentWrapper.setKey(InvoicingKey.buildKey(invoiceId, paymentId));
        paymentWrapper.setPaymentRiskData(paymentRiskData);
        return paymentWrapper;
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
