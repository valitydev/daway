package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.WalletConfig;
import dev.vality.damsel.domain.WalletConfigObject;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.dominant.impl.WalletDaoImpl;
import dev.vality.daway.domain.tables.pojos.Wallet;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import dev.vality.geck.common.util.TypeUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WalletHandler extends AbstractDominantHandler<WalletConfigObject, Wallet, String> {

    private final WalletDaoImpl walletDao;

    public WalletHandler(WalletDaoImpl walletDao) {
        this.walletDao = walletDao;
    }

    @Override
    protected DomainObjectDao<Wallet, String> getDomainObjectDao() {
        return walletDao;
    }

    @Override
    protected WalletConfigObject getTargetObject() {
        return getDomainObject().getWalletConfig();
    }

    @Override
    protected String getTargetObjectRefId() {
        return getTargetObject().getRef().getId();
    }

    @Override
    protected String getTargetRefId() {
        return getReference().getWalletConfig().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetWalletConfig();
    }

    @Override
    public Wallet convertToDatabaseObject(WalletConfigObject walletConfigObjectData,
                                          Long versionId,
                                          boolean current,
                                          String createdAt) {
        Wallet wallet = new Wallet();
        WalletConfig data = walletConfigObjectData.getData();
        wallet.setId(null);
        wallet.setWtime(null);
        wallet.setDominantVersionId(versionId);
        LocalDateTime createAt = TypeUtil.stringToLocalDateTime(createdAt);
        wallet.setEventCreatedAt(createAt);
        wallet.setPartyId(data.getPartyRef().id);
        wallet.setWalletId(walletConfigObjectData.getRef().id);
        wallet.setWalletName(data.getName());
        wallet.setCurrencyCode(data.getAccount().getCurrency().getSymbolicCode());
        wallet.setCurrent(current);
        wallet.setPaymentInstitutionId(data.getPaymentInstitution().getId());
        wallet.setBlocked(data.getBlock().isSetBlocked());
        wallet.setActive(data.getSuspension().isSetActive());
        return wallet;
    }
}
