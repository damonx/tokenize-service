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

/**
 * Cache configuration for the Tokenization service.
 *
 * <p>This configuration defines and registers a Caffeine-backed cache used for
 * token-to-account-number resolution. The cache is primarily used by the
 * {@code /detokenize} flow to avoid repeated database lookups for frequently
 * accessed tokens.</p>
 *
 * <p>The cache characteristics (initial capacity, maximum size and TTL) are
 * externalised via {@link TokenCacheProperties} to allow tuning without code
 * changes.</p>
 *
 * <p>Eviction and removal events are logged for operational visibility, and
 * cache statistics are recorded to support monitoring and performance analysis.</p>
 */
@Configuration
public class CacheConfiguration
{
    private static final Logger logger = LogManager.getLogger(CacheConfiguration.class);

    /**
     * Creates and configures the application's {@link CacheManager}.
     *
     * <p>This cache manager registers a custom Caffeine cache named
     * {@code "tokenToAccount"}, which stores mappings between generated tokens
     * and their corresponding account numbers.</p>
     *
     * <p>The cache uses a time-based eviction policy (expire-after-write),
     * size limits, and a system scheduler to ensure timely eviction of entries.
     * Cache eviction and removal events are logged to assist with debugging and
     * operational monitoring.</p>
     *
     * @param tokenCacheProperties configuration properties defining cache size,
     *                             TTL and initial capacity
     * @return a fully configured {@link CacheManager} instance
     */
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
