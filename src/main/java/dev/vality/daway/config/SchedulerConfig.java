package dev.vality.daway.config;

import dev.vality.damsel.domain_config.RepositorySrv;
import dev.vality.daway.handler.dominant.DominantPoller;
import dev.vality.daway.service.DominantService;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
public class SchedulerConfig {

    public static final String TABLE_NAME = "dw.shedlock";

    @Bean
    public DominantPoller dominantPoller(RepositorySrv.Iface dominantClient,
                                         DominantService dominantService,
                                         @Value("${dmt.polling.maxQuerySize}") int maxQuerySize,
                                         @Value("${dmt.polling.enabled}") boolean pollingEnabled) {
        return new DominantPoller(dominantClient, dominantService, maxQuerySize, pollingEnabled);
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource, TABLE_NAME);
    }

}
