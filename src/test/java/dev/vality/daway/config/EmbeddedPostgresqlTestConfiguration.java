package dev.vality.daway.config;

import com.zaxxer.hikari.HikariDataSource;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@TestConfiguration(proxyBeanMethods = false)
public class EmbeddedPostgresqlTestConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    HikariDataSource dataSource() {
        var dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(PostgresHolder.POSTGRES.getJdbcUrl("postgres", "postgres"));
        dataSource.setUsername("postgres");
        dataSource.setPassword("");
        return dataSource;
    }

    private static class PostgresHolder {

        private static final EmbeddedPostgres POSTGRES = startPostgres();

        private static EmbeddedPostgres startPostgres() {
            try {
                return EmbeddedPostgres.start();
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to start embedded PostgreSQL for tests", ex);
            }
        }
    }
}
