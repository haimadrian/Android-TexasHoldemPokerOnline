package org.hit.android.haim.chat.server.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.util.Arrays;

/**
 * A class to configure Spring boot.<br/>
 * We customize cache manager to be transaction aware.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Configuration
@EnableCaching
public class SpringBootConfiguration {
    @Bean
    public CacheManager cacheManager() {
        // We have our own cache manager settings in order to avoid of synchronization issues of the cache and the DB.
        // The problem is that we must make the cache transaction aware. Otherwise we might get into situations where the
        // cache was updated, but transaction failed. In this way we will look at the data in memory, but it will be lost
        // because that data was not persisted.
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache("userCache"), new ConcurrentMapCache("channelCache"), new ConcurrentMapCache("messageCache")));

        // Manually call initialize the caches as our SimpleCacheManager is not declared as a bean
        cacheManager.initializeCaches();

        return new TransactionAwareCacheManagerProxy(cacheManager);
    }

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(100000);
        loggingFilter.setIncludeHeaders(true);
        return loggingFilter;
    }
}

