package dev.vality.daway.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.vality.daway.model.PartyShop;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, PartyShop> partyShopDataCache(@Value("${cache.party-shop.size}") int cacheSize,
                                                       @Value("${cache.party-shop.expire.after.sec}") long expireAfter) {
        return Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(Duration.ofSeconds(expireAfter))
                .build();
    }

}
