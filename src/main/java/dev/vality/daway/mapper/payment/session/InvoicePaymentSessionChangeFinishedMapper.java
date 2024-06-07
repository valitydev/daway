package dev.vality.daway.mapper.payment.session;

import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentChange;
import dev.vality.damsel.payment_processing.SessionResult;
import dev.vality.daway.dao.invoicing.iface.PaymentRouteDao;
import dev.vality.daway.domain.enums.PaymentSessionResult;
import dev.vality.daway.domain.enums.PaymentSessionStatus;
import dev.vality.daway.domain.tables.pojos.PaymentRoute;
import dev.vality.daway.domain.tables.pojos.PaymentSessionInfo;
import dev.vality.daway.mapper.Mapper;
import dev.vality.daway.model.InvoicingKey;
import dev.vality.daway.model.PaymentWrapper;
import dev.vality.daway.util.JsonUtil;
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
public class InvoicePaymentSessionChangeFinishedMapper implements Mapper<PaymentWrapper> {

    private final PaymentRouteDao paymentRouteDao;

    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_session_change.payload.session_finished",
            new IsNullCondition().not()));

    @Override
    public PaymentWrapper map(InvoiceChange change, MachineEvent event, Integer changeId) {
        InvoicePaymentChange invoicePaymentChange = change.getInvoicePaymentChange();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePaymentChange.getId();
        long sequenceId = event.getEventId();
        log.info(
                "Start mapping session change finished info, sequenceId='{}', changeId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, changeId, invoiceId, paymentId);
        PaymentSessionInfo paymentSessionInfo = new PaymentSessionInfo();
        paymentSessionInfo.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        paymentSessionInfo.setInvoiceId(invoiceId);
        paymentSessionInfo.setPaymentId(paymentId);
        paymentSessionInfo.setSequenceId(sequenceId);
        paymentSessionInfo.setChangeId(changeId);
        paymentSessionInfo.setSessionStatus(PaymentSessionStatus.finished);

        SessionResult result =
                invoicePaymentChange.getPayload().getInvoicePaymentSessionChange().getPayload().getSessionFinished()
                        .getResult();
        PaymentRoute paymentRoute = paymentRouteDao.safeGet(invoiceId, paymentId);

        if (paymentRoute != null) {
            paymentSessionInfo.setPaymentTerminal(paymentRoute.getRouteTerminalId());
        }
        if (result.isSetFailed()) {
            paymentSessionInfo.setPaymentSessionResult(PaymentSessionResult.failed);
            paymentSessionInfo.setReason(JsonUtil.thriftBaseToJsonString(result.getFailed()));
        } else {
            paymentSessionInfo.setPaymentSessionResult(PaymentSessionResult.succeeded);
        }

        log.info(
                "Payment session finished has been mapped, sequenceId='{}', changeId='{}', invoiceId='{}', paymentId='{}'",
                sequenceId, changeId, invoiceId, paymentId);
        PaymentWrapper paymentWrapper = new PaymentWrapper();
        paymentWrapper.setKey(InvoicingKey.buildKey(invoiceId, paymentId));
        paymentWrapper.setPaymentSessionInfo(paymentSessionInfo);
        return paymentWrapper;
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
