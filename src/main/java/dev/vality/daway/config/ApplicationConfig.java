package dev.vality.daway.config;

import dev.vality.damsel.domain_config.RepositorySrv;
import dev.vality.daway.domain.Dw;
import dev.vality.woody.thrift.impl.http.THSpawnClientBuilder;
import org.jooq.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class ApplicationConfig {

    @Bean
    public Schema schema() {
        return Dw.DW;
    }
}
