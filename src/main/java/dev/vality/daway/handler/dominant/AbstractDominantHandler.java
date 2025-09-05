package dev.vality.daway.handler.dominant;

import dev.vality.damsel.domain.DomainObject;
import dev.vality.damsel.domain.Reference;
import dev.vality.damsel.domain_config_v2.FinalOperation;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @param <T> - damsel object class (CategoryObject, CurrencyObject etc.)
 * @param <C> - jooq object class (Category, Currency etc.)
 * @param <I> - object reference id class (Integer, String etc.)
 */
public abstract class AbstractDominantHandler<T, C, I> implements DominantHandler<FinalOperation> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String UNKNOWN_TYPE_EX = "Unknown type of operation. Only insert/update/remove supports. " +
                                                  "Operation: ";

    @Getter
    @Setter
    private DomainObject domainObject;
    @Getter
    @Setter
    private Reference reference;

    protected abstract DomainObjectDao<C, I> getDomainObjectDao();

    protected abstract T getTargetObject();

    protected abstract I getTargetObjectRefId();

    protected abstract I getTargetRefId();

    protected abstract boolean acceptDomainObject();

    public abstract C convertToDatabaseObject(T object, Long versionId, boolean current, String createdAt);

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(FinalOperation operation, Long versionId, String createdAt) {
        T object = getTargetObject();
        if (operation.isSetInsert()) {
            insertDomainObject(object, versionId, createdAt);
        } else if (operation.isSetUpdate()) {
            updateDomainObject(object, versionId, createdAt);
        } else if (operation.isSetRemove()) {
            removeDomainObject(object, versionId, createdAt);
        } else {
            throw new IllegalStateException(
                    UNKNOWN_TYPE_EX + operation);
        }
    }

    @Override
    public boolean acceptAndSet(FinalOperation operation) {
        if (operation.isSetInsert()) {
            setDomainObject(operation.getInsert().getObject());
        } else if (operation.isSetUpdate()) {
            setDomainObject(operation.getUpdate().getObject());
        } else if (operation.isSetRemove()) {
            setReference(operation.getRemove().getRef());
        } else {
            throw new IllegalStateException(
                    UNKNOWN_TYPE_EX + operation);
        }
        return acceptDomainObject();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertDomainObject(T object, Long versionId, String createdAt) {
        log.info("Start to insert '{}' with id={}, versionId={}", object.getClass().getSimpleName(),
                getTargetObjectRefId(), versionId);
        getDomainObjectDao().save(convertToDatabaseObject(object, versionId, true, createdAt));
        log.info("End to insert '{}' with id={}, versionId={}", object.getClass().getSimpleName(),
                getTargetObjectRefId(), versionId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateDomainObject(T object, Long versionId, String createdAt) {
        log.info("Start to update '{}' with id={}, versionId={}", object.getClass().getSimpleName(),
                getTargetObjectRefId(), versionId);
        getDomainObjectDao().updateNotCurrent(getTargetObjectRefId());
        getDomainObjectDao().save(convertToDatabaseObject(object, versionId, true, createdAt));
        log.info("End to update '{}' with id={}, versionId={}", object.getClass().getSimpleName(),
                getTargetObjectRefId(), versionId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void removeDomainObject(T object, Long versionId, String createdAt) {
        log.info("Start to remove '{}' with id={}, versionId={}", object.getClass().getSimpleName(),
                getTargetRefId(), versionId);
        getDomainObjectDao().updateNotCurrent(getTargetRefId());
        getDomainObjectDao().save(convertToDatabaseObject(object, versionId, false, createdAt));
        log.info("End to remove '{}' with id={}, versionId={}", object.getClass().getSimpleName(),
                getTargetRefId(), versionId);
    }
}
