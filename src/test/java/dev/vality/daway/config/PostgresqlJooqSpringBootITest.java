package dev.vality.daway.config;

import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(EmbeddedPostgresqlTestConfiguration.class)
@JooqTest
public @interface PostgresqlJooqSpringBootITest {
}
