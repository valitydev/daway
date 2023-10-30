package dev.vality.daway.util;

import dev.vality.damsel.domain.*;

import java.util.Optional;
import java.util.function.Supplier;

public class PaymentMethodUtils {

    private static final String TOKENIZED_BANK_CARD_SEPARATOR = "_";
    private static final String EMPTY_CVV = "empty_cvv_";

    public static Optional<String> getPaymentMethodRefIdByBankCard(
            Supplier<Optional<PaymentMethod>> paymentMethod) {
        return paymentMethod.get()
                .filter(PaymentMethod::isSetBankCard)
                .map(PaymentMethod::getBankCard)
                .flatMap(bankCard -> Optional.ofNullable(bankCard.getPaymentSystem()).map(PaymentSystemRef::getId));
    }

    public static Optional<String> getPaymentMethodRefIdByPaymentTerminal(
            Supplier<Optional<PaymentMethod>> paymentMethod) {
        return paymentMethod.get()
                .filter(PaymentMethod::isSetPaymentTerminal)
                .map(PaymentMethod::getPaymentTerminal)
                .map(PaymentServiceRef::getId);
    }

    public static Optional<String> getPaymentMethodRefIdByDigitalWallet(
            Supplier<Optional<PaymentMethod>> paymentMethod) {
        return paymentMethod.get()
                .filter(PaymentMethod::isSetDigitalWallet)
                .map(PaymentMethod::getDigitalWallet)
                .map(PaymentServiceRef::getId);
    }

    public static Optional<String> getPaymentMethodRefIdByCryptoCurrency(
            Supplier<Optional<PaymentMethod>> paymentMethod) {
        return paymentMethod.get()
                .filter(PaymentMethod::isSetCryptoCurrency)
                .map(PaymentMethod::getCryptoCurrency)
                .map(CryptoCurrencyRef::getId);
    }

    public static Optional<String> getPaymentMethodRefIdByMobile(
            Supplier<Optional<PaymentMethod>> paymentMethod) {
        return paymentMethod.get()
                .filter(PaymentMethod::isSetMobile)
                .map(PaymentMethod::getMobile)
                .map(MobileOperatorRef::getId);
    }

    public static Optional<String> getPaymentMethodRefIdByGeneric(
            Supplier<Optional<PaymentMethod>> paymentMethod) {
        return paymentMethod.get()
                .filter(PaymentMethod::isSetGeneric)
                .map(PaymentMethod::getGeneric)
                .map(GenericPaymentMethod::getPaymentService)
                .map(PaymentServiceRef::getId);
    }
}
