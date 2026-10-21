package com.srs.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * 缓存配置: Redis 可用时使用 Redis, 不可用(或未部署)时自动降级为本地缓存, 保证业务可运行
 */
@Slf4j
@Configuration
public class CacheConfig {

    @Value("${srs.cache.enabled:true}")
    private boolean cacheEnabled;

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        if (!cacheEnabled) {
            log.warn("[cache] 缓存已关闭(srs.cache.enabled=false), 使用本地缓存兜底");
            return localCacheManager();
        }
        try {
            redisConnectionFactory.getConnection().ping();
        } catch (Exception e) {
            log.warn("[cache] Redis 不可用({}), 已降级为本地缓存, 功能不受影响", e.getMessage());
            return localCacheManager();
        }

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        log.info("[cache] 使用 Redis 缓存");
        return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(config).build();
    }

    private CacheManager localCacheManager() {
        return new ConcurrentMapCacheManager();
    }

    public CacheType type() {
        return CacheType.REDIS;
    }
}
