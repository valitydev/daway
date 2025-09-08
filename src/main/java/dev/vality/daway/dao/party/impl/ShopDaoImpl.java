package dev.vality.daway.dao.party.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
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
public class ShopDaoImpl extends AbstractGenericDao implements DomainObjectDao<Shop, String> {

    private final RowMapper<Shop> rowMapper;

    public ShopDaoImpl(DataSource dataSource) {
        super(dataSource);
        rowMapper = new RecordRowMapper<>(SHOP, Shop.class);
    }

    @Override
    public Long save(Shop shop) throws DaoException {
        ShopRecord record = getDslContext().newRecord(SHOP, shop);
        Query query = getDslContext().insertInto(SHOP).set(record)
                .onConflict(SHOP.PARTY_ID, SHOP.SHOP_ID, SHOP.SEQUENCE_ID, SHOP.CHANGE_ID, SHOP.CLAIM_EFFECT_ID)
                .doNothing()
                .returning(SHOP.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue).get();
    }

    @Override
    public void updateNotCurrent(String id) throws DaoException {
        Query query = getDslContext()
                .update(SHOP).set(SHOP.CURRENT, false)
                .where(SHOP.SHOP_ID.eq(id));
        execute(query);
    }

    public Shop get(String partyId, String shopId) throws DaoException {
        Query query = getDslContext().selectFrom(SHOP)
                .where(SHOP.PARTY_ID.eq(partyId).and(SHOP.SHOP_ID.eq(shopId)).and(SHOP.CURRENT));
        Shop shop = fetchOne(query, rowMapper);
        if (shop == null) {
            throw new NotFoundException(String.format("Shop not found, shopId='%s'", shopId));
        }
        return shop;
    }
}
