package dev.vality.daway.dao.dominant.impl;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.domain.Tables;
import dev.vality.daway.domain.tables.pojos.Category;
import dev.vality.daway.domain.tables.records.CategoryRecord;
import dev.vality.daway.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class CategoryDaoImpl extends AbstractGenericDao implements DomainObjectDao<Category, Integer> {

    public CategoryDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long save(Category category) throws DaoException {
        CategoryRecord categoryRecord = getDslContext().newRecord(Tables.CATEGORY, category);
        Query query = getDslContext().insertInto(Tables.CATEGORY).set(categoryRecord).returning(Tables.CATEGORY.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void updateNotCurrent(Integer categoryId) throws DaoException {
        Query query = getDslContext().update(Tables.CATEGORY).set(Tables.CATEGORY.CURRENT, false)
                .where(Tables.CATEGORY.CATEGORY_REF_ID.eq(categoryId).and(Tables.CATEGORY.CURRENT));
        executeOne(query);
    }
}
