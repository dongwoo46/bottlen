package com.bottlen.bottlen_webflux.infra.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.output.IntegerOutput
import io.lettuce.core.output.StatusOutput
import io.lettuce.core.protocol.CommandArgs
import io.lettuce.core.protocol.ProtocolKeyword
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withContext
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
    redisClient: RedisClient
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val connection = redisClient.connect()
    private val commands = connection.coroutines()

    suspend fun init(
        filterName: String,
        errorRate: Double = 0.01,
        capacity: Long = 300_000
    ) {
        val args = CommandArgs(StringCodec.UTF8)
            .add(filterName)
            .add(errorRate.toString())
            .add(capacity.toString())

        try {
            commands.dispatch(
                BloomCommand.RESERVE,
                StatusOutput(StringCodec.UTF8),
                args
            ).single()
            log.info("Bloom filter created: $filterName")
        } catch (e: Exception) {
            if (e.message?.contains("exists") == true) {
                log.debug("Bloom filter already exists: $filterName")
            } else {
                log.warn("Failed to create bloom filter: $filterName", e)
                // throw 하지 않음 - Service가 판단하도록
            }
        }
    }

    /**
     * 아이템 추가 및 중복 확인
     * @return true: 새로 추가됨, false: 이미 존재했음
     */
    suspend fun add(filterName: String, key: String): Boolean {
        val args = CommandArgs(StringCodec.UTF8)
            .add(filterName)
            .add(key)

        val result = commands.dispatch(
            BloomCommand.ADD,
            IntegerOutput(StringCodec.UTF8),
            args
        ).single()

        return result == 1L
    }

    /**
     * 디버깅/관리 용도로만 사용
     * 실제 비즈니스 로직에서는 add() 사용 권장
     */
    @Deprecated("Use add() instead for atomicity")
    suspend fun exists(filterName: String, key: String): Boolean {
        val args = CommandArgs(StringCodec.UTF8)
            .add(filterName)
            .add(key)

        val result = commands.dispatch(
            BloomCommand.EXISTS,
            IntegerOutput(StringCodec.UTF8),
            args
        ).single()

        return result == 1L
    }

    @PreDestroy
    fun cleanup() {
        connection.close()
        log.info("Bloom filter client connection closed")
    }
}
