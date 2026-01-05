package com.bottlen.bottlen_webflux.infra.redis

import io.lettuce.core.ClientOptions
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.support.ConnectionPoolSupport
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class RedisConfig {

    @Bean(destroyMethod = "shutdown")
    fun redisClient(): RedisClient {
        val client = RedisClient.create("redis://localhost:6379")

        client.setOptions(
            ClientOptions.builder()
            .autoReconnect(true)
            .pingBeforeActivateConnection(true)
            .build()
        )

        return client
    }

    @Bean(destroyMethod = "close")
    fun redisConnectionPool(redisClient: RedisClient): GenericObjectPool<StatefulRedisConnection<String, String>> {

        val poolConfig = GenericObjectPoolConfig<StatefulRedisConnection<String, String>>().apply {

            // Redis TCP 커넥션 최대 개수
            maxTotal = 8

            // 유휴 커넥션 상한 / 하한
            maxIdle = 4
            minIdle = 2

            // 풀 고갈 시 최대 대기 시간 (fail-fast)
            setMaxWait(Duration.ofSeconds(2))  // 변경

            // 커넥션 사용 전 / idle 상태 헬스체크
            testOnBorrow = true
            testWhileIdle = true

            // idle 커넥션 정리 주기 / 기준
            setTimeBetweenEvictionRuns(Duration.ofSeconds(30))  // 변경
            setMinEvictableIdleTime(Duration.ofMinutes(1))
        }

        return ConnectionPoolSupport.createGenericObjectPool(
            { redisClient.connect(StringCodec.UTF8) },
            poolConfig
        )
    }
}
