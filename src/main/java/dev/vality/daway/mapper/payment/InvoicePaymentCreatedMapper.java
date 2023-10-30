package dev.vality.daway.mapper.payment;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.payment_processing.InvoiceChange;
import dev.vality.damsel.payment_processing.InvoicePaymentStarted;
import dev.vality.daway.domain.enums.PayerType;
import dev.vality.daway.domain.enums.PaymentChangeType;
import dev.vality.daway.domain.enums.PaymentFlowType;
import dev.vality.daway.domain.enums.PaymentToolType;
import dev.vality.daway.domain.tables.pojos.Payment;
import dev.vality.daway.domain.tables.pojos.PaymentPayerInfo;
import dev.vality.daway.factory.cash.flow.CashFlowFactory;
import dev.vality.daway.factory.cash.flow.CashFlowLinkFactory;
import dev.vality.daway.factory.invoice.payment.PaymentFeeFactory;
import dev.vality.daway.factory.invoice.payment.PaymentRouteFactory;
import dev.vality.daway.factory.invoice.payment.PaymentStatusInfoFactory;
import dev.vality.daway.mapper.Mapper;
import dev.vality.daway.model.CashFlowWrapper;
import dev.vality.daway.model.InvoicingKey;
import dev.vality.daway.model.PartyShop;
import dev.vality.daway.model.PaymentWrapper;
import dev.vality.daway.service.PartyShopCacheService;
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
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentCreatedMapper implements Mapper<PaymentWrapper> {

    private final PartyShopCacheService partyShopCacheService;

    private Filter filter = new PathConditionFilter(new PathConditionRule(
            "invoice_payment_change.payload.invoice_payment_started",
            new IsNullCondition().not()));

    @Override
    public PaymentWrapper map(InvoiceChange invoiceChange, MachineEvent event, Integer changeId) {
        InvoicePaymentStarted invoicePaymentStarted = invoiceChange
                .getInvoicePaymentChange()
                .getPayload()
                .getInvoicePaymentStarted();

        InvoicePayment invoicePayment = invoicePaymentStarted.getPayment();

        long sequenceId = event.getEventId();
        String invoiceId = event.getSourceId();
        String paymentId = invoicePayment.getId();
        log.info("Start payment created mapping, sequenceId={}, changeId={}, invoiceId={}, paymentId={}",
                sequenceId, changeId, invoiceId, paymentId);

        PartyShop partyShop = partyShopCacheService.get(invoiceId);
        if (partyShop == null) {
            log.info("PartyShop not found for invoiceId = {}", invoiceId);
            return null;
        }

        PaymentWrapper paymentWrapper = new PaymentWrapper();
        paymentWrapper.setKey(InvoicingKey.buildKey(invoiceId, paymentId));
        LocalDateTime eventCreatedAt = TypeUtil.stringToLocalDateTime(event.getCreatedAt());
        paymentWrapper.setPayment(getPayment(
                invoicePayment,
                invoiceId,
                paymentId,
                partyShop,
                eventCreatedAt,
                changeId,
                sequenceId
        ));
        paymentWrapper.setPaymentStatusInfo(PaymentStatusInfoFactory.build(
                invoicePayment.getStatus(),
                invoiceId,
                paymentId,
                eventCreatedAt,
                changeId,
                sequenceId
        ));
        paymentWrapper.setPaymentPayerInfo(getPayerPaymentInfo(
                invoicePayment,
                invoiceId,
                paymentId,
                eventCreatedAt,
                changeId,
                sequenceId
        ));
        if (invoicePaymentStarted.isSetRoute()) {
            paymentWrapper.setPaymentRoute(PaymentRouteFactory.build(
                    invoicePaymentStarted.getRoute(),
                    invoiceId,
                    paymentId,
                    TypeUtil.stringToLocalDateTime(event.getCreatedAt()),
                    changeId,
                    sequenceId
            ));
        }
        if (invoicePaymentStarted.isSetCashFlow()) {
            paymentWrapper.setCashFlowWrapper(new CashFlowWrapper(
                    CashFlowLinkFactory.build(paymentId, invoiceId, eventCreatedAt, changeId, sequenceId),
                    CashFlowFactory.build(invoicePaymentStarted.getCashFlow(), null, PaymentChangeType.payment)
            ));
            paymentWrapper.setPaymentFee(PaymentFeeFactory.build(
                    invoicePaymentStarted.getCashFlow(),
                    invoiceId,
                    paymentId,
                    eventCreatedAt,
                    changeId,
                    sequenceId
            ));
        }
        log.info("Payment has been mapped, sequenceId={}, changeId={}, invoiceId={}, paymentId={}",
                sequenceId, changeId, invoiceId, paymentId);
        return paymentWrapper;
    }

    private Payment getPayment(InvoicePayment invoicePayment,
                               String invoiceId,
                               String paymentId,
                               PartyShop partyShop,
                               LocalDateTime eventCreatedAt,
                               Integer changeId,
                               Long sequenceId) {
        Payment payment = new Payment();
        payment.setEventCreatedAt(eventCreatedAt);
        payment.setPaymentId(paymentId);
        payment.setInvoiceId(invoiceId);
        payment.setCreatedAt(TypeUtil.stringToLocalDateTime(invoicePayment.getCreatedAt()));
        payment.setPartyId(partyShop.getPartyId());
        payment.setShopId(partyShop.getShopId());
        payment.setDomainRevision(invoicePayment.getDomainRevision());
        if (invoicePayment.isSetPartyRevision()) {
            payment.setPartyRevision(invoicePayment.getPartyRevision());
        }
        payment.setAmount(invoicePayment.getCost().getAmount());
        payment.setCurrencyCode(invoicePayment.getCost().getCurrency().getSymbolicCode());
        if (invoicePayment.isSetMakeRecurrent()) {
            payment.setMakeRecurrent(invoicePayment.isMakeRecurrent());
        }
        payment.setExternalId(invoicePayment.getExternalId());
        payment.setPaymentFlowType(TBaseUtil.unionFieldToEnum(invoicePayment.getFlow(), PaymentFlowType.class));
        if (invoicePayment.getFlow().isSetHold()) {
            payment.setPaymentFlowHeldUntil(
                    TypeUtil.stringToLocalDateTime(invoicePayment.getFlow().getHold().getHeldUntil()));
            payment.setPaymentFlowOnHoldExpiration(invoicePayment.getFlow().getHold().getOnHoldExpiration().name());
        }
        payment.setSequenceId(sequenceId);
        payment.setChangeId(changeId);
        return payment;
    }

    private PaymentPayerInfo getPayerPaymentInfo(InvoicePayment invoicePayment,
                                                 String invoiceId,
                                                 String paymentId,
                                                 LocalDateTime eventCreatedAt,
                                                 Integer changeId,
                                                 Long sequenceId) {
        Payer payer = invoicePayment.getPayer();
        PaymentPayerInfo payerInfo = new PaymentPayerInfo();
        payerInfo.setInvoiceId(invoiceId);
        payerInfo.setPaymentId(paymentId);
        payerInfo.setEventCreatedAt(eventCreatedAt);
        payerInfo.setPayerType(TBaseUtil.unionFieldToEnum(payer, PayerType.class));
        if (payer.isSetPaymentResource()) {
            PaymentResourcePayer paymentResource = payer.getPaymentResource();
            fillPaymentTool(payerInfo, paymentResource.getResource().getPaymentTool());
            fillContactInfo(payerInfo, paymentResource.getContactInfo());
            if (paymentResource.getResource().isSetClientInfo()) {
                payerInfo.setIpAddress(paymentResource.getResource().getClientInfo().getIpAddress());
                payerInfo.setFingerprint(paymentResource.getResource().getClientInfo().getFingerprint());
            }
        } else if (payer.isSetCustomer()) {
            CustomerPayer customer = payer.getCustomer();
            payerInfo.setCustomerId(customer.getCustomerId());
            payerInfo.setCustomerBindingId(customer.getCustomerBindingId());
            payerInfo.setCustomerRecPaymentToolId(customer.getRecPaymentToolId());
            fillPaymentTool(payerInfo, customer.getPaymentTool());
            fillContactInfo(payerInfo, customer.getContactInfo());
        } else if (payer.isSetRecurrent()) {
            payerInfo.setRecurrentParentInvoiceId(payer.getRecurrent().getRecurrentParent().getInvoiceId());
            payerInfo.setRecurrentParentPaymentId(payer.getRecurrent().getRecurrentParent().getPaymentId());
            fillPaymentTool(payerInfo, payer.getRecurrent().getPaymentTool());
            fillContactInfo(payerInfo, payer.getRecurrent().getContactInfo());
        }
        payerInfo.setSequenceId(sequenceId);
        payerInfo.setChangeId(changeId);
        return payerInfo;
    }

    private void fillContactInfo(PaymentPayerInfo payerInfo, ContactInfo contactInfo) {
        payerInfo.setPhoneNumber(contactInfo.getPhoneNumber());
        payerInfo.setEmail(contactInfo.getEmail());
    }

    private void fillPaymentTool(PaymentPayerInfo payerInfo, PaymentTool paymentTool) {
        payerInfo.setPaymentToolType(TBaseUtil.unionFieldToEnum(paymentTool, PaymentToolType.class));
        if (paymentTool.isSetBankCard()) {
            BankCard bankCard = paymentTool.getBankCard();
            payerInfo.setBankCardToken(bankCard.getToken());
            payerInfo.setBankCardPaymentSystem(Optional.ofNullable(bankCard.getPaymentSystem())
                    .map(PaymentSystemRef::getId).orElse(null));
            payerInfo.setBankCardBin(bankCard.getBin());
            payerInfo.setBankCardMaskedPan(bankCard.getLastDigits());
            payerInfo.setBankName(bankCard.getBankName());
            payerInfo.setBankCardCardholderName(bankCard.getCardholderName());
            if (bankCard.isSetIssuerCountry()) {
                payerInfo.setIssuerCountry(bankCard.getIssuerCountry().name());
            }
            payerInfo.setBankCardTokenProvider(Optional.ofNullable(bankCard.getPaymentToken())
                    .map(BankCardTokenServiceRef::getId).orElse(null));
        } else if (paymentTool.isSetPaymentTerminal()) {
            payerInfo.setPaymentTerminalType(
                    Optional.ofNullable(paymentTool.getPaymentTerminal().getPaymentService())
                            .map(PaymentServiceRef::getId).orElse(null));
        } else if (paymentTool.isSetDigitalWallet()) {
            payerInfo.setDigitalWalletId(paymentTool.getDigitalWallet().getId());
            payerInfo.setDigitalWalletProvider(
                    Optional.ofNullable(paymentTool.getDigitalWallet().getPaymentService())
                            .map(PaymentServiceRef::getId).orElse(null));
        } else if (paymentTool.isSetCryptoCurrency()) {
            payerInfo.setCryptoCurrencyType(Optional.ofNullable(paymentTool.getCryptoCurrency())
                    .map(CryptoCurrencyRef::getId).orElse(null));
        } else if (paymentTool.isSetMobileCommerce()) {
            payerInfo.setMobileOperator(paymentTool.getMobileCommerce().getOperator().getId());
            payerInfo.setMobilePhoneCc(paymentTool.getMobileCommerce().getPhone().getCc());
            payerInfo.setMobilePhoneCtn(paymentTool.getMobileCommerce().getPhone().getCtn());
        }
    }

    @Override
    public Filter<InvoiceChange> getFilter() {
        return filter;
    }
}
