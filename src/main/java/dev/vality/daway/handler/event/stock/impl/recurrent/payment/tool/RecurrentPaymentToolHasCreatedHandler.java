package dev.vality.daway.handler.event.stock.impl.recurrent.payment.tool;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.payment_processing.RecurrentPaymentToolChange;
import dev.vality.damsel.payment_processing.RecurrentPaymentToolHasCreated;
import dev.vality.daway.dao.recurrent.payment.tool.iface.RecurrentPaymentToolDao;
import dev.vality.daway.domain.enums.PaymentToolType;
import dev.vality.daway.domain.enums.RecurrentPaymentToolStatus;
import dev.vality.daway.domain.tables.pojos.RecurrentPaymentTool;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.util.JsonUtil;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurrentPaymentToolHasCreatedHandler implements RecurrentPaymentToolHandler {

    private final RecurrentPaymentToolDao recurrentPaymentToolDao;
    private final MachineEventCopyFactory<RecurrentPaymentTool, Integer> recurrentPaymentToolCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("rec_payment_tool_created", new IsNullCondition().not()));

    @Override
    public void handle(RecurrentPaymentToolChange change, MachineEvent event, Integer changeId) {
        long sequenceId = event.getEventId();
        log.info("Start recurrent payment tool created handling, sourceId={}, sequenceId={}, changeId={}",
                event.getSourceId(), sequenceId, changeId);
        RecurrentPaymentToolHasCreated recPaymentToolCreated = change.getRecPaymentToolCreated();
        var recurrentPaymentToolOrigin = recPaymentToolCreated.getRecPaymentTool();
        RecurrentPaymentTool recurrentPaymentTool =
                recurrentPaymentToolCopyFactory.create(event, sequenceId, changeId, null);
        recurrentPaymentTool.setRecurrentPaymentToolId(event.getSourceId());
        recurrentPaymentTool.setCreatedAt(TypeUtil.stringToLocalDateTime(recurrentPaymentToolOrigin.getCreatedAt()));
        recurrentPaymentTool.setPartyId(recurrentPaymentToolOrigin.getPartyId());
        recurrentPaymentTool.setShopId(recurrentPaymentToolOrigin.getShopId());
        recurrentPaymentTool.setPartyRevision(recurrentPaymentToolOrigin.getPartyRevision());
        recurrentPaymentTool.setDomainRevision(recurrentPaymentToolOrigin.getDomainRevision());
        recurrentPaymentTool.setStatus(
                TBaseUtil.unionFieldToEnum(recurrentPaymentToolOrigin.getStatus(), RecurrentPaymentToolStatus.class));
        DisposablePaymentResource paymentResource = recurrentPaymentToolOrigin.getPaymentResource();
        fillPaymentTool(recurrentPaymentTool, paymentResource.getPaymentTool());
        recurrentPaymentTool.setPaymentSessionId(paymentResource.getPaymentSessionId());
        if (paymentResource.isSetClientInfo()) {
            recurrentPaymentTool.setClientInfoIpAddress(paymentResource.getClientInfo().getIpAddress());
            recurrentPaymentTool.setClientInfoFingerprint(paymentResource.getClientInfo().getFingerprint());
        }
        recurrentPaymentTool.setRecToken(recurrentPaymentToolOrigin.getRecToken());
        if (recurrentPaymentToolOrigin.isSetRoute()) {
            recurrentPaymentTool.setRouteProviderId(recurrentPaymentToolOrigin.getRoute().getProvider().getId());
            recurrentPaymentTool.setRouteTerminalId(recurrentPaymentToolOrigin.getRoute().getTerminal().getId());
        }
        if (recPaymentToolCreated.isSetRoute()) {
            recurrentPaymentTool.setRouteProviderId(recPaymentToolCreated.getRoute().getProvider().getId());
            recurrentPaymentTool.setRouteTerminalId(recPaymentToolCreated.getRoute().getTerminal().getId());
        }
        if (recurrentPaymentToolOrigin.isSetMinimalPaymentCost()) {
            recurrentPaymentTool.setAmount(recurrentPaymentToolOrigin.getMinimalPaymentCost().getAmount());
            recurrentPaymentTool.setCurrencyCode(
                    recurrentPaymentToolOrigin.getMinimalPaymentCost().getCurrency().getSymbolicCode());
        }
        if (recPaymentToolCreated.isSetRiskScore()) {
            recurrentPaymentTool.setRiskScore(recPaymentToolCreated.getRiskScore().name());
        }
        recurrentPaymentToolDao.save(recurrentPaymentTool);
        log.info("End recurrent payment tool created handling, sourceId={}, sequenceId={}, changeId={}",
                event.getSourceId(), sequenceId, changeId);
    }

    private void fillPaymentTool(RecurrentPaymentTool recurrentPaymentTool, PaymentTool paymentTool) {
        recurrentPaymentTool.setPaymentToolType(TBaseUtil.unionFieldToEnum(paymentTool, PaymentToolType.class));
        if (paymentTool.isSetBankCard()) {
            fillBankCard(recurrentPaymentTool, paymentTool);
        } else if (paymentTool.isSetPaymentTerminal()) {
            fillPaymentTerminal(recurrentPaymentTool, paymentTool);
        } else if (paymentTool.isSetDigitalWallet()) {
            fillDigitalWallet(recurrentPaymentTool, paymentTool);
        } else if (paymentTool.isSetCryptoCurrency()) {
            fillCryptoCurrency(recurrentPaymentTool, paymentTool);
        } else if (paymentTool.isSetMobileCommerce()) {
            fillMobileCommerce(recurrentPaymentTool, paymentTool);
        }
    }

    private void fillMobileCommerce(RecurrentPaymentTool recurrentPaymentTool, PaymentTool paymentTool) {
        recurrentPaymentTool.setMobileCommerceOperator(
                paymentTool.getMobileCommerce().getOperator().getId());
        recurrentPaymentTool.setMobileCommercePhoneCc(paymentTool.getMobileCommerce().getPhone().getCc());
        recurrentPaymentTool.setMobileCommercePhoneCtn(paymentTool.getMobileCommerce().getPhone().getCtn());
    }

    private void fillCryptoCurrency(RecurrentPaymentTool recurrentPaymentTool, PaymentTool paymentTool) {
        recurrentPaymentTool.setCryptoCurrency(paymentTool.getCryptoCurrency().getId());
    }

    private void fillDigitalWallet(RecurrentPaymentTool recurrentPaymentTool, PaymentTool paymentTool) {
        DigitalWallet digitalWallet = paymentTool.getDigitalWallet();
        recurrentPaymentTool.setDigitalWalletId(digitalWallet.getId());
        recurrentPaymentTool.setDigitalWalletProvider(Optional.ofNullable(digitalWallet.getPaymentService())
                .map(PaymentServiceRef::getId).orElse(null));
        recurrentPaymentTool.setDigitalWalletToken(digitalWallet.getToken());
    }

    private void fillPaymentTerminal(RecurrentPaymentTool recurrentPaymentTool, PaymentTool paymentTool) {
        recurrentPaymentTool.setPaymentTerminalType(
                paymentTool.getPaymentTerminal().getPaymentService().getId());
    }

    private void fillBankCard(RecurrentPaymentTool recurrentPaymentTool, PaymentTool paymentTool) {
        BankCard bankCard = paymentTool.getBankCard();
        recurrentPaymentTool.setBankCardToken(bankCard.getToken());
        recurrentPaymentTool.setBankCardPaymentSystem(Optional.ofNullable(bankCard.getPaymentSystem())
                .map(PaymentSystemRef::getId).orElse(null));
        recurrentPaymentTool.setBankCardBin(bankCard.getBin());
        recurrentPaymentTool.setBankCardMaskedPan(bankCard.getLastDigits());
        recurrentPaymentTool.setBankCardTokenProvider(Optional.ofNullable(bankCard.getPaymentToken())
                .map(BankCardTokenServiceRef::getId).orElse(null));
        if (bankCard.isSetIssuerCountry()) {
            recurrentPaymentTool.setBankCardIssuerCountry(bankCard.getIssuerCountry().name());
        }
        recurrentPaymentTool.setBankCardBankName(bankCard.getBankName());
        if (bankCard.isSetMetadata()) {
            recurrentPaymentTool.setBankCardMetadataJson(JsonUtil.objectToJsonString(
                    bankCard.getMetadata().entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> JsonUtil.thriftBaseToJsonNode(e.getValue())
                    ))));
        }
        if (bankCard.isSetIsCvvEmpty()) {
            recurrentPaymentTool.setBankCardIsCvvEmpty(bankCard.isIsCvvEmpty());
        }
        if (bankCard.isSetExpDate()) {
            recurrentPaymentTool.setBankCardExpDateMonth((int) bankCard.getExpDate().getMonth());
            recurrentPaymentTool.setBankCardExpDateYear((int) bankCard.getExpDate().getYear());
        }
        recurrentPaymentTool.setBankCardCardholderName(bankCard.getCardholderName());
    }

    @Override
    public Filter<RecurrentPaymentToolChange> getFilter() {
        return filter;
    }
}
