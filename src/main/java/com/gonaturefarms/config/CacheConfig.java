package com.gonaturefarms.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleCacheManager;

import java.util.Arrays;

/**
 * Cache configuration for homepage data
 * Uses in-memory cache for simplicity (suitable for single-instance deployment)
 * Cache duration: 5 minutes for homepage data
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("homepageData")
        ));
        return cacheManager;
    }
}
