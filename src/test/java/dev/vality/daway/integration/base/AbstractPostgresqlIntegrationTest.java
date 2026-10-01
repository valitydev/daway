package dev.vality.daway.integration.base;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.sql.DataSource;
import java.io.IOException;

public abstract class AbstractPostgresqlIntegrationTest {

    private static final EmbeddedPostgres EMBEDDED_POSTGRES = startPostgres();
    private static final String JDBC_URL = EMBEDDED_POSTGRES.getJdbcUrl("postgres", "postgres");

    @Autowired
    private DataSource dataSource;

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> JDBC_URL);
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "");
    }

    @BeforeEach
    void cleanDatabase() {
        var jdbcTemplate = new JdbcTemplate(dataSource);
        var tables = jdbcTemplate.queryForList("""
                SELECT format('%I.%I', n.nspname, c.relname)
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = 'dw'
                  AND c.relkind IN ('r', 'p')
                  AND c.relname NOT IN ('flyway_schema_history', 'schema_version')
                  AND NOT EXISTS (SELECT 1 FROM pg_inherits i WHERE i.inhrelid = c.oid)
                """, String.class);
        if (!tables.isEmpty()) {
            jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", tables) + " CASCADE");
        }
    }

    private static EmbeddedPostgres startPostgres() {
        try {
            return EmbeddedPostgres.start();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start embedded PostgreSQL for tests", ex);
        }
    }
}
