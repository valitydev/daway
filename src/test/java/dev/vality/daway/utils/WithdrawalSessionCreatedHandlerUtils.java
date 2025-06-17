package dev.vality.daway.utils;

import dev.vality.fistful.base.*;
import dev.vality.fistful.withdrawal_session.*;
import dev.vality.kafka.common.serialization.ThriftSerializer;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.machinegun.msgpack.Value;

public class WithdrawalSessionCreatedHandlerUtils {

    public static final String WITHDRAWAL_ID = "withdrawal_id";
    public static final String SESSION_ID = "session_id";

    public static final int PROVIDER_ID = 1;
    public static final int TERMINAL_ID = 1;

    public static final String DIGITAL_WALLET_ID = "digital_wallet_id";
    public static final String CRYPTO_WALLET_ID = "crypto_wallet_id";

    public static final String CARD_BIN = "bin";
    public static final String CARD_MASKED_PAN = "1232132";
    public static final String CARD_TOKEN_PROVIDER = "cardToken";

    public static MachineEvent createCreatedMachineEvent(String id,
                                                         Session session) {
        return new MachineEvent()
                .setEventId(2L)
                .setSourceId(id)
                .setSourceNs("2")
                .setCreatedAt("2021-05-31T06:12:27Z")
                .setData(Value.bin(new ThriftSerializer<>().serialize("", createCreated(session))));
    }

    public static TimestampedChange createCreated(Session session) {
        return new TimestampedChange()
                .setOccuredAt("2021-05-31T06:12:27Z")
                .setChange(Change.created(session));
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

    public static ResourceDigitalWallet createDestinationResourceDigitalWallet() {
        ResourceDigitalWallet resourceDigitalWallet = new ResourceDigitalWallet();
        resourceDigitalWallet.setDigitalWallet(createFistfulDigitalWallet());
        return resourceDigitalWallet;
    }

    public static ResourceCryptoWallet createDestinationResourceCryptoWallet() {
        ResourceCryptoWallet resourceCryptoWallet = new ResourceCryptoWallet();
        resourceCryptoWallet.setCryptoWallet(createFistfulCryptoWallet());
        return resourceCryptoWallet;
    }

    public static ResourceBankCard createDestinationResourceBankCard() {
        ResourceBankCard resourceBankCard = new ResourceBankCard();
        resourceBankCard.setBankCard(createFistfulBankCard());
        return resourceBankCard;
    }

    public static Session createSession(Resource resource) {
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setId(WITHDRAWAL_ID);
        withdrawal.setDestinationResource(resource);

        Cash cash = new Cash();
        cash.setAmount(11L);
        cash.setCurrency(new CurrencyRef("RUB"));
        withdrawal.setCash(cash);
        Session session = new Session();
        session.setId(SESSION_ID);
        session.setWithdrawal(withdrawal);
        session.setRoute(createRoute());
        session.setStatus(SessionStatus.active(new SessionActive()));
        return session;
    }

    public static Route createRoute() {
        Route route = new Route();
        route.setProviderId(PROVIDER_ID);
        route.setTerminalId(TERMINAL_ID);
        return route;
    }

}
