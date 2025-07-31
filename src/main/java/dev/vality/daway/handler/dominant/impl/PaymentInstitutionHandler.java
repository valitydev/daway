package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.PaymentInstitutionObject;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.dominant.impl.PaymentInstitutionDaoImpl;
import dev.vality.daway.domain.tables.pojos.PaymentInstitution;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import dev.vality.daway.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PaymentInstitutionHandler
        extends AbstractDominantHandler<PaymentInstitutionObject, PaymentInstitution, Integer> {

    private final PaymentInstitutionDaoImpl paymentInstitutionDao;

    public PaymentInstitutionHandler(PaymentInstitutionDaoImpl paymentInstitutionDao) {
        this.paymentInstitutionDao = paymentInstitutionDao;
    }

    @Override
    protected DomainObjectDao<PaymentInstitution, Integer> getDomainObjectDao() {
        return paymentInstitutionDao;
    }

    @Override
    protected PaymentInstitutionObject getTargetObject() {
        return getDomainObject().getPaymentInstitution();
    }

    @Override
    protected Integer getTargetObjectRefId() {
        return getTargetObject().getRef().getId();
    }

    @Override
    protected Integer getTargetRefId() {
        return getReference().getPaymentInstitution().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetPaymentInstitution();
    }

    @Override
    public PaymentInstitution convertToDatabaseObject(PaymentInstitutionObject paymentInstitutionObject, Long versionId,
                                                      boolean current) {
        PaymentInstitution paymentInstitution = new PaymentInstitution();
        paymentInstitution.setVersionId(versionId);
        paymentInstitution.setPaymentInstitutionRefId(getTargetObjectRefId());
        dev.vality.damsel.domain.PaymentInstitution data = paymentInstitutionObject.getData();
        paymentInstitution.setName(data.getName());
        paymentInstitution.setDescription(data.getDescription());
        if (data.isSetCalendar()) {
            paymentInstitution.setCalendarRefId(data.getCalendar().getId());
        }
        paymentInstitution.setSystemAccountSetJson(JsonUtil.thriftBaseToJsonString(data.getSystemAccountSet()));
        paymentInstitution
                .setDefaultContractTemplateJson(JsonUtil.thriftBaseToJsonString(data.getDefaultContractTemplate()));
        if (data.isSetProviders()) {
            paymentInstitution.setProvidersJson(JsonUtil.thriftBaseToJsonString(data.getProviders()));
        }
        paymentInstitution.setInspectorJson(JsonUtil.thriftBaseToJsonString(data.getInspector()));
        paymentInstitution.setRealm(data.getRealm().name());
        paymentInstitution.setResidencesJson(
                JsonUtil.objectToJsonString(data.getResidences().stream().map(Enum::name).collect(Collectors.toSet())));
        paymentInstitution.setCurrent(current);
        return paymentInstitution;
    }
}
