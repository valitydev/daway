package dev.vality.daway.utils;

import dev.vality.daway.util.JsonUtil;
import dev.vality.fistful.base.*;
import dev.vality.fistful.destination.Change;
import dev.vality.fistful.destination.Destination;
import dev.vality.fistful.destination.TimestampedChange;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.msgpack.Value;
import dev.vality.swag.wallets.model.wallets.BankRU;
import dev.vality.swag.wallets.model.wallets.DestinationResource;
import dev.vality.swag.wallets.model.wallets.SBPTransfer;

public class DestinationHandlerTestUtils {

    public static final String DESTINATION_NAME = "name";

    public static final String DIGITAL_WALLET_ID = "digital_wallet_id";
    public static final String CRYPTO_WALLET_ID = "crypto_wallet_id";

    public static final String CARD_BIN = "bin";
    public static final String CARD_MASKED_PAN = "1232132";
    public static final String CARD_TOKEN_PROVIDER = "cardToken";

    public static final String PHONE_NUMBER = "79111111111";

    public static MachineEvent createCreatedMachineEvent(String id, Destination destination) {
        return new MachineEvent()
                .setEventId(2L)
                .setSourceId(id)
                .setSourceNs("2")
                .setCreatedAt("2021-05-31T06:12:27Z")
                .setData(Value.bin(new ThriftSerializer<>().serialize("", createCreated(destination))));
    }

    public static TimestampedChange createCreated(Destination destination) {
        return new TimestampedChange()
                .setOccuredAt("2021-05-31T06:12:27Z")
                .setChange(Change.created(destination));
    }

    public static DigitalWallet createFistfulDigitalWallet() {
        DigitalWallet digitalWallet = new DigitalWallet();
        digitalWallet.setId(DIGITAL_WALLET_ID);
        digitalWallet.setPaymentService(new PaymentServiceRef("webmoney"));
        return digitalWallet;
    }

    public static CryptoWallet createFistfulCryptoWallet() {
        CryptoWallet cryptoWallet = new CryptoWallet();
        cryptoWallet.setId(CRYPTO_WALLET_ID);
        cryptoWallet.setCurrency(new CryptoCurrencyRef("bitcoin"));
        return cryptoWallet;
    }

    public static BankCard createFistfulBankCard() {
        BankCard bankCard = new BankCard();
        bankCard.setToken(CARD_TOKEN_PROVIDER);
        bankCard.setBin(CARD_BIN);
        bankCard.setMaskedPan(CARD_MASKED_PAN);
        return bankCard;
    }

    public static ResourceDigitalWallet createResourceDigitalWallet() {
        ResourceDigitalWallet resourceDigitalWallet = new ResourceDigitalWallet();
        resourceDigitalWallet.setDigitalWallet(DestinationHandlerTestUtils.createFistfulDigitalWallet());
        return resourceDigitalWallet;
    }

    public static ResourceCryptoWallet createResourceCryptoWallet() {
        ResourceCryptoWallet resourceCryptoWallet = new ResourceCryptoWallet();
        resourceCryptoWallet.setCryptoWallet(DestinationHandlerTestUtils.createFistfulCryptoWallet());
        return resourceCryptoWallet;
    }

    public static ResourceBankCard createResourceBankCard() {
        ResourceBankCard resourceBankCard = new ResourceBankCard();
        resourceBankCard.setBankCard(DestinationHandlerTestUtils.createFistfulBankCard());
        return resourceBankCard;
    }

    public static Destination createFistfulDestination(Resource fistfulResource) {
        Destination fistfulDestination
                = new Destination();
        fistfulDestination.setResource(fistfulResource);
        fistfulDestination.setName(DESTINATION_NAME);
        return fistfulDestination;
    }

    public static SBPTransfer createSbpTransfer() {
        SBPTransfer sbpTransfer = new SBPTransfer();
        sbpTransfer.setType(DestinationResource.TypeEnum.SBPTRANSFER);
        BankRU bankRU = new BankRU();
        bankRU.setBankCode(BankRU.BankCodeEnum.WB);
        sbpTransfer.setBank(bankRU);
        sbpTransfer.setPhoneNumber(PHONE_NUMBER);
        return sbpTransfer;
    }

    public static ResourceGeneric createResourceGenericSBPTransfer() {
        ResourceGeneric resourceGeneric = new ResourceGeneric();
        ResourceGenericData resourceGenericData = new ResourceGenericData();
        resourceGenericData.setProvider(new PaymentServiceRef().setId("provider"));
        resourceGenericData.setData(new Content()
                .setType("transfer")
                .setData(JsonUtil.toBytes(createSbpTransfer())));
        resourceGeneric.setGeneric(resourceGenericData);
        return resourceGeneric;
    }

}
