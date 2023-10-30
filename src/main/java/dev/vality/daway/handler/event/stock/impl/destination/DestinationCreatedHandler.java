package dev.vality.daway.handler.event.stock.impl.destination;

import com.fasterxml.jackson.databind.JsonNode;
import dev.vality.daway.dao.destination.iface.DestinationDao;
import dev.vality.daway.domain.enums.DestinationResourceType;
import dev.vality.daway.domain.enums.DestinationStatus;
import dev.vality.daway.domain.tables.pojos.Destination;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.daway.util.JsonUtil;
import dev.vality.fistful.base.*;
import dev.vality.fistful.destination.Change;
import dev.vality.fistful.destination.TimestampedChange;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.swag.wallets.model.wallets.DestinationResource;
import dev.vality.swag.wallets.model.wallets.SBPTransfer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationCreatedHandler implements DestinationHandler {

    private final DestinationDao destinationDao;
    private final MachineEventCopyFactory<Destination, String> destinationMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.created", new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        long sequenceId = event.getEventId();
        Change change = timestampedChange.getChange();
        String destinationId = event.getSourceId();
        log.info("Start destination created handling, sequenceId={}, destinationId={}", sequenceId, destinationId);

        Destination destination = destinationMachineEventCopyFactory
                .create(event, sequenceId, destinationId, timestampedChange.getOccuredAt());

        destination.setDestinationName(change.getCreated().getName());
        destination.setDestinationStatus(DestinationStatus.unauthorized);
        destination.setExternalId(change.getCreated().getExternalId());

        //TODO почему так было сделано?
        destination.setIdentityId(destinationId);
        if (change.getCreated().isSetCreatedAt()) {
            destination.setCreatedAt(TypeUtil.stringToLocalDateTime(change.getCreated().getCreatedAt()));
        }
        if (change.getCreated().isSetMetadata()) {
            Map<String, JsonNode> jsonNodeMap = change.getCreated().getMetadata().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> JsonUtil.thriftBaseToJsonNode(e.getValue())));
            destination.setContextJson(JsonUtil.objectToJsonString(jsonNodeMap));
        }
        Resource resource = change.getCreated().getResource();
        destination.setResourceType(TBaseUtil.unionFieldToEnum(resource, DestinationResourceType.class));
        if (resource.isSetBankCard()) {
            ResourceBankCard resourceBankCard = resource.getBankCard();
            BankCard bankCard = resourceBankCard.getBankCard();
            destination.setResourceBankCardToken(bankCard.getToken());
            destination.setResourceBankCardBin(bankCard.getBin());
            destination.setResourceBankCardMaskedPan(bankCard.getMaskedPan());
            destination.setResourceBankCardBankName(bankCard.getBankName());
            if (bankCard.isSetIssuerCountry()) {
                destination.setResourceBankCardIssuerCountry(bankCard.getIssuerCountry().toString());
            }
            if (bankCard.isSetPaymentSystem()) {
                destination.setResourceBankCardPaymentSystem(bankCard.getPaymentSystem().toString());
            }
            if (bankCard.isSetCardType()) {
                destination.setResourceBankCardType(bankCard.getCardType().toString());
            }
        } else if (resource.isSetCryptoWallet()) {
            ResourceCryptoWallet resourceCryptoWallet = resource.getCryptoWallet();
            var cryptoWallet = resourceCryptoWallet.getCryptoWallet();
            destination.setResourceCryptoWalletId(cryptoWallet.getId());
            destination.setResourceCryptoWalletType(cryptoWallet.getCurrency().getId());
        } else if (resource.isSetDigitalWallet()) {
            ResourceDigitalWallet resourceDigitalWallet = resource.getDigitalWallet();
            DigitalWallet digitalWallet = resourceDigitalWallet.getDigitalWallet();
            destination.setResourceDigitalWalletId(digitalWallet.getId());
            if (digitalWallet.isSetPaymentService()) {
                destination.setResourceDigitalWalletData(digitalWallet.getPaymentService().getId());
            }
        } else if (resource.isSetGeneric()) {
            ResourceGeneric generic = resource.getGeneric();
            Content data = generic.getGeneric().getData();
            DestinationResource destinationResource = JsonUtil.stringToObject(data.getData(), DestinationResource.class);
            if (destinationResource instanceof SBPTransfer sbpTransfer) {
                destination.setResourceGenericSbpTransferPhone(sbpTransfer.getPhoneNumber());
            }
        }

        destinationDao.save(destination).ifPresentOrElse(
                dbContractId -> log.info("Destination created has been saved, sequenceId={}, destinationId={}",
                        sequenceId, destinationId),
                () -> log.info("Destination created bound duplicated,, sequenceId={}, destinationId={}",
                        sequenceId, destinationId));
    }

}
