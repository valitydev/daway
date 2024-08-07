package dev.vality.daway.dao.party.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.party.iface.ShopDao;
import dev.vality.daway.domain.tables.pojos.Shop;
import dev.vality.daway.domain.tables.records.ShopRecord;
import dev.vality.daway.exception.DaoException;
import dev.vality.daway.exception.NotFoundException;
import dev.vality.mapper.RecordRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static dev.vality.daway.domain.Tables.SHOP;

@Slf4j
@Component
public class ShopDaoImpl extends AbstractGenericDao implements ShopDao {

    private final RowMapper<Shop> shopRowMapper;

    public ShopDaoImpl(DataSource dataSource) {
        super(dataSource);
        shopRowMapper = new RecordRowMapper<>(SHOP, Shop.class);
    }

    @Override
    public Optional<Long> save(Shop shop) throws DaoException {
        ShopRecord record = getDslContext().newRecord(SHOP, shop);
        Query query = getDslContext().insertInto(SHOP).set(record)
                .onConflict(SHOP.PARTY_ID, SHOP.SHOP_ID, SHOP.SEQUENCE_ID, SHOP.CHANGE_ID, SHOP.CLAIM_EFFECT_ID)
                .doNothing()
                .returning(SHOP.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public Shop get(String partyId, String shopId) throws DaoException {
        Query query = getDslContext().selectFrom(SHOP)
                .where(SHOP.PARTY_ID.eq(partyId).and(SHOP.SHOP_ID.eq(shopId)).and(SHOP.CURRENT));
        return Optional.ofNullable(fetchOne(query, shopRowMapper))
                .orElseThrow(() -> new NotFoundException(String.format("Shop not found, shopId='%s'", shopId)));
    }

    @Override
    public void updateNotCurrent(Long id) throws DaoException {
        Query query = getDslContext()
                .update(SHOP).set(SHOP.CURRENT, false)
                .where(SHOP.ID.eq(id));
        executeOne(query);
    }

    @Override
    public void saveWithUpdateCurrent(Shop shopSource, Long oldEventId, String eventName) {
        save(shopSource).ifPresentOrElse(
                atLong -> {
                    updateNotCurrent(oldEventId);
                    log.info("Shop {} has been saved, sequenceId={}, partyId={}, shopId={}, changeId={}",
                            eventName, shopSource.getSequenceId(), shopSource.getPartyId(), shopSource.getShopId(),
                            shopSource.getChangeId());
                },
                () -> log.info("Shop {} duplicated, sequenceId={}, partyId={}, shopId={}, changeId={}",
                        eventName, shopSource.getSequenceId(), shopSource.getPartyId(), shopSource.getShopId(),
                        shopSource.getChangeId())
        );
    }
}
