package dev.vality.daway.utils;

import dev.vality.daway.util.JsonUtil;
import dev.vality.fistful.base.*;
import dev.vality.fistful.destination.Change;
import dev.vality.fistful.destination.Destination;
import dev.vality.fistful.destination.TimestampedChange;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.msgpack.Value;

public class DestinationHandlerTestUtils {

    public static final String DESTINATION_NAME = "name";
    public static final String DESTINATION_ID = "1";
    public static final String PARTY_ID = "1";

    public static final String DIGITAL_WALLET_ID = "digital_wallet_id";
    public static final String CRYPTO_WALLET_ID = "crypto_wallet_id";

    public static final String CARD_BIN = "bin";
    public static final String CARD_MASKED_PAN = "1232132";
    public static final String CARD_TOKEN_PROVIDER = "cardToken";

    public static final String PHONE_NUMBER = "79111111111";
    public static final String CREATED_AT = "2021-05-31T06:12:27Z";

    public static MachineEvent createCreatedMachineEvent(String id, Destination destination) {
        return new MachineEvent()
                .setEventId(2L)
                .setSourceId(id)
                .setSourceNs("2")
                .setCreatedAt(CREATED_AT)
                .setData(Value.bin(new ThriftSerializer<>().serialize("", createCreated(destination))));
    }

    public static TimestampedChange createCreated(Destination destination) {
        return new TimestampedChange()
                .setOccuredAt(CREATED_AT)
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
        fistfulDestination.setId(DESTINATION_ID);
        fistfulDestination.setPartyId(PARTY_ID);
        fistfulDestination.setRealm(Realm.test);
        fistfulDestination.setCreatedAt(CREATED_AT);
        return fistfulDestination;
    }

    public static String createSbpTransfer() {
        return """
                {
                  "type": "SBPTransfer",
                  "bank": {
                    "bankCode": "WB"
                  },
                  "phoneNumber": "79111111111"
                }
                """;
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
