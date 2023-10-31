package dev.vality.daway.service;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.limiter.config.LimitConfig;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.mapper.RecordRowMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.vality.daway.dao.LimitConfigDaoTest.SELECT_CURRENT;
import static dev.vality.daway.domain.tables.LimitConfig.LIMIT_CONFIG;
import static dev.vality.daway.utils.LimitConfigGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

@PostgresqlSpringBootITest
public class LimitConfigServiceTest {

    private static final String TABLE_NAME = LIMIT_CONFIG.getSchema().getName() + "." + LIMIT_CONFIG.getName();

    @Autowired
    private LimitConfigService limitConfigService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void shouldHandleAndSave() {
        var limitConfigId = UUID.randomUUID().toString();
        var limitConfig = getLimitConfig(limitConfigId);
        limitConfigService.handleEvents(List.of(
                getMachineEvent(limitConfigId, limitConfig), getMachineEvent(UUID.randomUUID().toString())));
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_NAME))
                .isEqualTo(2);
        var saved = selectCurrent(limitConfigId);
        assertThat(saved.getShardSize())
                .isEqualTo(limitConfig.getShardSize());
    }

    @Test
    public void shouldHandleAndSaveWithZeroScope() {
        var limitConfigId = UUID.randomUUID().toString();
        var limitConfig = getLimitConfig(limitConfigId);
        limitConfig.getScope().setMulti(Set.of());
        limitConfigService.handleEvents(List.of(getMachineEvent(limitConfigId, limitConfig)));
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_NAME))
                .isEqualTo(1);
        var saved = selectCurrent(limitConfigId);
        assertThat(saved.getShardSize())
                .isEqualTo(limitConfig.getShardSize());
    }

    private dev.vality.daway.domain.tables.pojos.LimitConfig selectCurrent(String limitConfigId) {
        return jdbcTemplate.queryForObject(
                SELECT_CURRENT,
                new RecordRowMapper<>(LIMIT_CONFIG, dev.vality.daway.domain.tables.pojos.LimitConfig.class),
                limitConfigId);
    }

    private MachineEvent getMachineEvent(String limitConfigId) {
        return getMachineEvent(limitConfigId, getLimitConfig(limitConfigId));
    }

    private MachineEvent getMachineEvent(String limitConfigId, LimitConfig limitConfig) {
        return getEvent(
                limitConfigId,
                1,
                getCreated(limitConfig));
    }
}
