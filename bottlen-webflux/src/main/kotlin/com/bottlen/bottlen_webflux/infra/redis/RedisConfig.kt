package com.bottlen.bottlen_webflux.infra.redis

import io.lettuce.core.RedisClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedisConfig {

    @Bean
    fun redisClient(): RedisClient {
        // Redis Stack 기본 포트: 6379
        return RedisClient.create("redis://localhost:6379")
    }
}
