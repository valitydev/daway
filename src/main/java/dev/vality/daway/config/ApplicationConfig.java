package dev.vality.daway.config;

import dev.vality.daway.domain.Dw;
import org.jooq.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public Schema schema() {
        return Dw.DW;
    }
}
