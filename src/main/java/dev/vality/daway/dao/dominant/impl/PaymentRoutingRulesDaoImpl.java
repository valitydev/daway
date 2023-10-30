package dev.vality.daway.dao.dominant.impl;

import com.zaxxer.hikari.HikariDataSource;
import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.tables.pojos.PaymentRoutingRule;
import dev.vality.daway.domain.tables.records.PaymentRoutingRuleRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

@Component
public class PaymentRoutingRulesDaoImpl extends AbstractGenericDao
        implements DomainObjectDao<PaymentRoutingRule, Integer> {

    public PaymentRoutingRulesDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(PaymentRoutingRule routingRule) throws DaoException {
        PaymentRoutingRuleRecord record = getDslContext().newRecord(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE, routingRule);
        Query query = getDslContext()
                .insertInto(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE)
                .set(record)
                .returning(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer ruleId) throws DaoException {
        Query query = getDslContext()
                .update(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE)
                .set(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE.CURRENT, false)
                .where(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE.RULE_REF_ID.eq(ruleId).and(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE.CURRENT));
        execute(query);
    }
}
