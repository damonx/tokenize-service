package nz.co.anz.tokenization.configurations;

import nz.co.anz.tokenization.properties.TokenCacheProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfiguration
{
    private static final Logger logger = LogManager.getLogger(CacheConfiguration.class);

    @Bean
    public CacheManager cacheManager(final TokenCacheProperties tokenCacheProperties) {
        final CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        final Cache<Object, Object> tokenCache = Caffeine.newBuilder()
            .initialCapacity(tokenCacheProperties.getInitialCapacity())
            .maximumSize(tokenCacheProperties.getMaximumSize())
            .expireAfterWrite(tokenCacheProperties.getTtl())
            .evictionListener((key, value, cause) -> logger.info("Key '{}' was evicted ({}) from tokenCache", key, cause))
            .removalListener((key, value, cause) -> logger.info("Key '{}' was removed ({}) from tokenCache", key, cause))
            .scheduler(Scheduler.systemScheduler())
            .recordStats()
            .build();
        cacheManager.registerCustomCache("tokenToAccount", tokenCache);
        return cacheManager;
    }
}
