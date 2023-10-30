package dev.vality.daway.dao;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.dao.limiter.LimitConfigDao;
import dev.vality.daway.domain.tables.pojos.LimitConfig;
import dev.vality.daway.util.JsonUtil;
import dev.vality.limiter.config.LimitScopeType;
import dev.vality.mapper.RecordRowMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Set;
import java.util.stream.Collectors;

import static dev.vality.daway.domain.tables.LimitConfig.LIMIT_CONFIG;
import static dev.vality.daway.utils.LimitConfigGenerator.getLimitConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@PostgresqlSpringBootITest
public class LimitConfigDaoTest {

    public static final String SELECT_CURRENT = "select * from dw.limit_config where limit_config_id = ? and current = true;";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private LimitConfigDao limitConfigDao;

    @Test
    public void limitConfigDaoTest() {
        var pojo = dev.vality.testcontainers.annotations.util.RandomBeans.random(LimitConfig.class);
        pojo.setCurrent(true);
        pojo.setLimitScopeTypesJson(getLimitScopeTypesJson(getLimitConfig(pojo.getLimitConfigId()).getScope().getMulti()));
        var id = limitConfigDao.save(pojo).get();
        pojo.setId(id);
        var limitConfigId = pojo.getLimitConfigId();
        var actual = selectCurrent(limitConfigId);
        assertEquals(pojo, actual);
        limitConfigDao.updateNotCurrent(actual.getId());
        limitConfigDao.save(pojo);
        assertThrows(EmptyResultDataAccessException.class, () -> selectCurrent(pojo.getLimitConfigId()));
    }

    private LimitConfig selectCurrent(String limitConfigId) {
        return jdbcTemplate.queryForObject(
                SELECT_CURRENT,
                new RecordRowMapper<>(LIMIT_CONFIG, LimitConfig.class),
                limitConfigId);
    }

    private String getLimitScopeTypesJson(Set<LimitScopeType> limitScopeTypes) {
        return JsonUtil.objectToJsonString(limitScopeTypes.stream()
                .map(JsonUtil::thriftBaseToJsonNode)
                .collect(Collectors.toList()));
    }
}
