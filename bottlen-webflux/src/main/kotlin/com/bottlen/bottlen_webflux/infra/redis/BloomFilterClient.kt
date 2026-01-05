package com.bottlen.bottlen_webflux.infra.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.output.IntegerOutput
import io.lettuce.core.output.StatusOutput
import io.lettuce.core.protocol.CommandArgs
import io.lettuce.core.protocol.ProtocolKeyword
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withContext
import org.apache.commons.pool2.impl.GenericObjectPool
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

enum class BloomCommand(val raw: String) : ProtocolKeyword {
    RESERVE("BF.RESERVE"),
    ADD("BF.ADD"),
    EXISTS("BF.EXISTS");

    override fun getBytes(): ByteArray = raw.toByteArray()
}
@OptIn(io.lettuce.core.ExperimentalLettuceCoroutinesApi::class)
@Component
class BloomFilterClient(
    private val pool: GenericObjectPool<StatefulRedisConnection<String, String>>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private suspend fun <T> withConnection(
        block: suspend (io.lettuce.core.api.coroutines.RedisCoroutinesCommands<String, String>) -> T
    ): T {
        val connection = pool.borrowObject()
        try {
            return block(connection.coroutines())
        } finally {
            pool.returnObject(connection)
        }
    }

    /**
     * Bloom Filter를 생성(초기화)한다.
     *
     * - 이미 존재하는 경우는 정상 상태로 간주한다.
     * - Redis 장애, 인증 실패 등 치명적인 오류는 Result.failure로 전달한다.
     * - Bloom Filter는 중복 제거를 위한 "보조 수단"이므로,
     *   실패하더라도 호출자가 정책적으로 처리할 수 있도록 예외를 던지지 않는다.
     *
     * @return
     *  - Result.success(Unit) : 생성 성공 또는 이미 존재
     *  - Result.failure(e)    : Redis 장애 등 치명적 실패
     */
    suspend fun init(
        filterName: String,
        errorRate: Double = 0.01,
        capacity: Long = 300_000
    ): Result<Unit> = withConnection { commands ->

        // BF.RESERVE <filter> <error_rate> <capacity>
        // Bloom Filter 생성 명령어 인자 구성
        val args = CommandArgs(StringCodec.UTF8)
            .add(filterName)
            .add(errorRate.toString())
            .add(capacity.toString())

        try {
            // Bloom Filter 생성 시도
            commands.dispatch(
                BloomCommand.RESERVE,
                StatusOutput(StringCodec.UTF8),
                args
            ).single()

            // 정상적으로 생성된 경우
            log.info("Bloom filter created: $filterName")
            Result.success(Unit)

        } catch (e: Exception) {

            // 이미 존재하는 경우 → 정상 상태로 간주
            if (e.message?.contains("exists") == true) {
                log.debug("Bloom filter already exists: $filterName")
                Result.success(Unit)

            } else {
                // Redis 장애, 인증 실패 등 치명적 오류
                log.error("Failed to create bloom filter: $filterName", e)
                Result.failure(e)
            }
        }
    }


    /**
     * 아이템 추가 및 중복 확인
     * @return true: 새로 추가됨, false: 이미 존재했음
     */
    suspend fun add(filterName: String, key: String): Boolean =
        withConnection { commands ->

            val args = CommandArgs(StringCodec.UTF8)
                .add(filterName)
                .add(key)

            val result = commands.dispatch(
                BloomCommand.ADD,
                IntegerOutput(StringCodec.UTF8),
                args
            ).single()

            result == 1L
        }

    /**
     * 디버깅/관리 용도로만 사용
     * 실제 비즈니스 로직에서는 add() 사용 권장
     */
    @Deprecated("Use add() instead for atomicity")
    suspend fun exists(filterName: String, key: String): Boolean =
        withConnection { commands ->

            val args = CommandArgs(StringCodec.UTF8)
                .add(filterName)
                .add(key)

            val result = commands.dispatch(
                BloomCommand.EXISTS,
                IntegerOutput(StringCodec.UTF8),
                args
            ).single()

            result == 1L
        }
}
