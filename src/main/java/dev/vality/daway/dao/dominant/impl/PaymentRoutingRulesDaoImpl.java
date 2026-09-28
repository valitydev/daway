package dev.vality.daway.dao.dominant.impl;

import com.zaxxer.hikari.HikariDataSource;
import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.domain.tables.pojos.PaymentRoutingRule;
import dev.vality.daway.domain.tables.records.PaymentRoutingRuleRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentRoutingRulesDaoImpl extends AbstractGenericDao
        implements DomainObjectDao<PaymentRoutingRule, Integer> {

    public PaymentRoutingRulesDaoImpl(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(PaymentRoutingRule routingRule) throws DaoException {
        PaymentRoutingRuleRecord record =
                getDslContext().newRecord(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE,
                        routingRule);
        Query query = getDslContext()
                .insertInto(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE)
                .set(record)
                .onConflict(Tables.PAYMENT_ROUTING_RULE.RULE_REF_ID, Tables.PAYMENT_ROUTING_RULE.VERSION_ID)
                .doNothing()
                .returning(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue).orElse(null);
    }

    @Override
    public void updateNotCurrent(Integer ruleId) throws DaoException {
        Query query = getDslContext()
                .update(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE)
                .set(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE.CURRENT, false)
                .where(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE.RULE_REF_ID.eq(ruleId)
                        .and(dev.vality.daway.domain.tables.PaymentRoutingRule.PAYMENT_ROUTING_RULE.CURRENT));
        execute(query);
    }
}
