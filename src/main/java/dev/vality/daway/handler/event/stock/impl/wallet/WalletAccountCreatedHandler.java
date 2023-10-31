package dev.vality.daway.handler.event.stock.impl.wallet;

import dev.vality.daway.dao.identity.iface.IdentityDao;
import dev.vality.daway.dao.wallet.iface.WalletDao;
import dev.vality.daway.domain.tables.pojos.Identity;
import dev.vality.daway.domain.tables.pojos.Wallet;
import dev.vality.daway.factory.machine.event.MachineEventCopyFactory;
import dev.vality.fistful.account.Account;
import dev.vality.fistful.wallet.Change;
import dev.vality.fistful.wallet.TimestampedChange;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletAccountCreatedHandler implements WalletHandler {

    private final IdentityDao identityDao;
    private final WalletDao walletDao;
    private final MachineEventCopyFactory<Wallet, String> walletMachineEventCopyFactory;

    @Getter
    private final Filter filter = new PathConditionFilter(
            new PathConditionRule("change.account.created", new IsNullCondition().not()));

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(TimestampedChange timestampedChange, MachineEvent event) {
        Change change = timestampedChange.getChange();
        Account account = change.getAccount().getCreated();
        long sequenceId = event.getEventId();
        String walletId = event.getSourceId();
        log.info("Start wallet account created handling, sequenceId={}, walletId={}",
                sequenceId, walletId);

        final Wallet walletOld = walletDao.get(walletId);
        Identity identity = identityDao.get(account.getIdentity());
        Wallet walletNew = walletMachineEventCopyFactory
                .create(event, sequenceId, walletId, walletOld, timestampedChange.getOccuredAt());
        walletNew.setIdentityId(account.getIdentity());
        walletNew.setAccountId(account.getId());
        walletNew.setPartyId(identity.getPartyId());
        walletNew.setAccounterAccountId(account.getAccounterAccountId());
        walletNew.setCurrencyCode(account.getCurrency().getSymbolicCode());

        walletDao.save(walletNew).ifPresentOrElse(
                id -> {
                    walletDao.updateNotCurrent(walletOld.getId());
                    log.info("Wallet account have been changed, sequenceId={}, walletId={}", sequenceId, walletId);
                },
                () -> log.info("Wallet account have been saved, sequenceId={}, walletId={}", sequenceId, walletId));
    }

}
