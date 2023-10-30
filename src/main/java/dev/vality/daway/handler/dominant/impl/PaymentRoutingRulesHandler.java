package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.RoutingRulesObject;
import dev.vality.damsel.domain.RoutingRuleset;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.tables.pojos.PaymentRoutingRule;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import dev.vality.daway.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentRoutingRulesHandler
        extends AbstractDominantHandler<RoutingRulesObject, PaymentRoutingRule, Integer> {

    private final DomainObjectDao<PaymentRoutingRule, Integer> paymentRoutingRulesDao;

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetRoutingRules();
    }

    @Override
    public PaymentRoutingRule convertToDatabaseObject(RoutingRulesObject rulesObject,
                                                      Long versionId,
                                                      boolean current) {

        PaymentRoutingRule paymentRoutingRule = new PaymentRoutingRule();
        paymentRoutingRule.setRuleRefId(rulesObject.getRef().getId());
        paymentRoutingRule.setVersionId(versionId);

        RoutingRuleset ruleset = rulesObject.getData();
        paymentRoutingRule.setName(ruleset.getName());
        paymentRoutingRule.setDescription(ruleset.getDescription());
        paymentRoutingRule.setRoutingDecisionsJson(JsonUtil.thriftBaseToJsonString(ruleset.getDecisions()));
        paymentRoutingRule.setCurrent(current);
        return paymentRoutingRule;
    }

    @Override
    protected DomainObjectDao<PaymentRoutingRule, Integer> getDomainObjectDao() {
        return paymentRoutingRulesDao;
    }

    @Override
    protected RoutingRulesObject getTargetObject() {
        return getDomainObject().getRoutingRules();
    }

    @Override
    protected Integer getTargetObjectRefId() {
        return getTargetObject().getRef().getId();
    }

}
