package dev.vality.daway.integration;

import dev.vality.daway.config.PostgresqlSpringBootITest;
import dev.vality.daway.integration.base.AbstractPostgresqlIntegrationTest;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PostgresqlSpringBootITest
class PostgresqlTestInfrastructureTest extends AbstractPostgresqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RepeatedTest(2)
    void databaseIsCleanForEachScenarioAndMigrationHistoryIsPreserved() {
        assertEquals(0, jdbcTemplate.queryForObject("SELECT count(*) FROM dw.currency", Integer.class));
        assertTrue(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM dw.flyway_schema_history WHERE success", Integer.class) > 0);
        jdbcTemplate.update("""
                INSERT INTO dw.currency (version_id, currency_ref_id, name, symbolic_code, numeric_code, exponent)
                VALUES (1, 'isolation-test', 'Test currency', 'TST', 999, 2)
                """);
        assertEquals(1, jdbcTemplate.queryForObject("SELECT count(*) FROM dw.currency", Integer.class));
    }
}
