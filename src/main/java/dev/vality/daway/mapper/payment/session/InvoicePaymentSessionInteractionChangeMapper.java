package dev.vality.daway.mapper.payment.session;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.user_interaction.BrowserHTTPRequest;
import dev.vality.damsel.user_interaction.UserInteraction;
import dev.vality.daway.domain.enums.PaymentSessionStatus;
import dev.vality.daway.domain.tables.pojos.PaymentSessionInfo;
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
public class InvoicePaymentSessionInteractionChangeMapper implements Mapper<PaymentWrapper> {

    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_session_change.payload.session_interaction_changed",
            new IsNullCondition().not()));

    @Override
    public PaymentWrapper map(InvoiceChange change, MachineEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePaymentChange.getId();
        long sequenceId = event.getEventId();
        log.info(
                "Start mapping session interaction change info, sequenceId='{}', changeId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, changeId, invoiceId, paymentId);
        PaymentSessionInfo paymentSessionInfo = new PaymentSessionInfo();
        paymentSessionInfo.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        paymentSessionInfo.setInvoiceId(invoiceId);
        paymentSessionInfo.setPaymentId(paymentId);
        paymentSessionInfo.setSequenceId(sequenceId);
        paymentSessionInfo.setChangeId(changeId);
        paymentSessionInfo.setUserInteraction(Boolean.TRUE);
        var invoicePaymentSessionChange = invoicePaymentChange.getPayload().getInvoicePaymentSessionChange();
        var userInteraction = invoicePaymentSessionChange.getPayload().getSessionInteractionChanged().getInteraction();
        if (userInteraction.isSetApiExtensionRequest()) {
            paymentSessionInfo.setSessionStatus(PaymentSessionStatus.interaction_changed_api_extension);
        } else if (userInteraction.isSetPaymentTerminalReciept()) {
            paymentSessionInfo.setSessionStatus(PaymentSessionStatus.interaction_changed_terminal_receipt);
        } else if (userInteraction.isSetQrCodeDisplayRequest()) {
            paymentSessionInfo.setSessionStatus(PaymentSessionStatus.interaction_changed_qr_display);
        } else if (userInteraction.isSetCryptoCurrencyTransferRequest()) {
            paymentSessionInfo.setSessionStatus(PaymentSessionStatus.interaction_changed_crypto_transfer);
        } else if (userInteraction.isSetRedirect()) {
            paymentSessionInfo.setSessionStatus(PaymentSessionStatus.interaction_changed_redirect);
            paymentSessionInfo.setUserInteractionUrl(getUserInteractionUrl(userInteraction));
        }
        log.info(
                "Payment session interaction change has been mapped, sequenceId='{}', changeId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, changeId, invoiceId, paymentId);
        PaymentWrapper paymentWrapper = new PaymentWrapper();
        paymentWrapper.setKey(InvoicingKey.buildKey(invoiceId, paymentId));
        paymentWrapper.setPaymentSessionInfo(paymentSessionInfo);
        return paymentWrapper;
    }

    private String getUserInteractionUrl(UserInteraction userInteraction) {
        BrowserHTTPRequest redirect = userInteraction.getRedirect();
        if (redirect.isSetGetRequest()) {
            return redirect.getGetRequest().getUri();
        } else {
            return redirect.getPostRequest().getUri();
        }
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
