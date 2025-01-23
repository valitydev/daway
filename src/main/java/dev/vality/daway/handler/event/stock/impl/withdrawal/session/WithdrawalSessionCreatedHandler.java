package dev.vality.daway.handler.event.stock.impl.withdrawal.session;

import dev.vality.daway.dao.withdrawal.session.iface.WithdrawalSessionDao;
import dev.vality.daway.domain.enums.DestinationResourceType;
import dev.vality.daway.domain.enums.WithdrawalSessionStatus;
import dev.vality.daway.domain.tables.pojos.WithdrawalSession;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.fistful.base.*;
import dev.vality.fistful.withdrawal_session.Change;
import dev.vality.fistful.withdrawal_session.Session;
import dev.vality.fistful.withdrawal_session.TimestampedChange;
import dev.vality.fistful.withdrawal_session.Withdrawal;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalSessionCreatedHandler implements WithdrawalSessionHandler {

    private final WithdrawalSessionDao withdrawalSessionDao;
    private final MachineEventCopyFactory<WithdrawalSession, String> withdrawalSessionMachineEventCopyFactory;

    @Getter
    private final Filter filter =
            new PathConditionFilter(new PathConditionRule("change.created", new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        long sequenceId = event.getEventId();
        String withdrawalSessionId = event.getSourceId();
        log.info("Start withdrawal session created handling, sequenceId={}, withdrawalId={}",
                sequenceId, withdrawalSessionId);

        WithdrawalSession withdrawalSession = withdrawalSessionMachineEventCopyFactory
                .create(event, sequenceId, withdrawalSessionId, timestampedChange.getOccuredAt());

        Session session = change.getCreated();
        withdrawalSession.setProviderId(session.getRoute().getProviderId());
        withdrawalSession.setProviderIdLegacy(session.getProviderLegacy());

        withdrawalSession.setWithdrawalSessionStatus(WithdrawalSessionStatus.active);

        Withdrawal withdrawal = session.getWithdrawal();
        withdrawalSession.setWithdrawalId(withdrawal.getId());

        Resource resource = withdrawal.getDestinationResource();
        withdrawalSession.setResourceType(TBaseUtil.unionFieldToEnum(resource, DestinationResourceType.class));
        if (resource.isSetBankCard()) {
            ResourceBankCard resourceBankCard = resource.getBankCard();
            BankCard bankCard = resourceBankCard.getBankCard();
            withdrawalSession.setDestinationCardToken(bankCard.getToken());
            withdrawalSession.setDestinationCardBin(bankCard.getBin());
            withdrawalSession.setDestinationCardMaskedPan(bankCard.getMaskedPan());
            if (bankCard.isSetPaymentSystem()) {
                withdrawalSession.setDestinationCardPaymentSystem(bankCard.getPaymentSystem().getId());
            }
            withdrawalSession.setResourceBankCardBankName(bankCard.getBankName());
            if (bankCard.isSetIssuerCountry()) {
                withdrawalSession.setResourceBankCardIssuerCountry(bankCard.getIssuerCountry().toString());
            }
            if (bankCard.isSetCardType()) {
                withdrawalSession.setResourceBankCardType(bankCard.getCardType().toString());
            }
        } else if (resource.isSetCryptoWallet()) {
            ResourceCryptoWallet resourceCryptoWallet = resource.getCryptoWallet();
            var cryptoWallet = resourceCryptoWallet.getCryptoWallet();
            withdrawalSession.setResourceCryptoWalletId(cryptoWallet.getId());
            withdrawalSession.setResourceCryptoWalletType(cryptoWallet.getCurrency().getId());
        } else if (resource.isSetDigitalWallet()) {
            ResourceDigitalWallet resourceDigitalWallet = resource.getDigitalWallet();
            DigitalWallet digitalWallet = resourceDigitalWallet.getDigitalWallet();
            withdrawalSession.setResourceDigitalWalletId(digitalWallet.getId());
            if (digitalWallet.isSetPaymentService()) {
                withdrawalSession.setResourceDigitalWalletData(digitalWallet.getPaymentService().getId());
            }
        }

        var transactionInfo = change.getTransactionBound().getTrxInfo();
        var additionalInfo = transactionInfo.getAdditionalInfo();
        if (transactionInfo.isSetAdditionalInfo()) {
            withdrawalSession.setTranAdditionalInfoRrn(additionalInfo.getRrn());
        }

        Cash cash = withdrawal.getCash();
        withdrawalSession.setAmount(cash.getAmount());
        withdrawalSession.setCurrencyCode(cash.getCurrency().getSymbolicCode());

        withdrawalSessionDao.save(withdrawalSession).ifPresentOrElse(
                id -> log.info("Withdrawal session created has been saved, sequenceId={}, withdrawalId={}",
                        sequenceId, withdrawalSessionId),
                () -> log.info("Withdrawal session created bound duplicated, sequenceId={}, withdrawalId={}",
                        sequenceId, withdrawalSessionId));

    }

}
