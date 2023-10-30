package dev.vality.daway.dao.limiter;

import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.daway.domain.tables.pojos.LimitConfig;
import dev.vality.daway.exception.DaoException;
import dev.vality.mapper.RecordRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static dev.vality.daway.domain.Tables.LIMIT_CONFIG;

@Component
public class LimitConfigDaoImpl extends AbstractGenericDao implements LimitConfigDao {

    private final RowMapper<LimitConfig> limitConfigRowMapper;

    @Autowired
    public LimitConfigDaoImpl(DataSource dataSource) {
        super(dataSource);
        limitConfigRowMapper = new RecordRowMapper<>(LIMIT_CONFIG, LimitConfig.class);
    }

    @Override
    public Optional<Long> save(LimitConfig limitConfig) throws DaoException {
        var limitConfigRecord = getDslContext().newRecord(LIMIT_CONFIG, limitConfig);
        var query = getDslContext()
                .insertInto(LIMIT_CONFIG)
                .set(limitConfigRecord)
                .onConflict(LIMIT_CONFIG.LIMIT_CONFIG_ID, LIMIT_CONFIG.SEQUENCE_ID)
                .doNothing()
                .returning(LIMIT_CONFIG.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public void updateNotCurrent(Long id) throws DaoException {
        var query = getDslContext().update(LIMIT_CONFIG).set(LIMIT_CONFIG.CURRENT, false)
                .where(LIMIT_CONFIG.ID.eq(id)
                        .and(LIMIT_CONFIG.CURRENT));
        executeOne(query);
    }
}
