package com.bottlen.bottlen_webflux.infra.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.output.IntegerOutput
import io.lettuce.core.output.StatusOutput
import io.lettuce.core.protocol.CommandArgs
import io.lettuce.core.protocol.ProtocolKeyword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withContext
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

enum class BloomCommand(val raw: String) : ProtocolKeyword {
    RESERVE("BF.RESERVE"),
    ADD("BF.ADD"),
    EXISTS("BF.EXISTS");

    override fun getBytes(): ByteArray = raw.toByteArray()
}

@Component
class BloomFilterClient(
        redisClient: RedisClient
) {

    private val connection = redisClient.connect()
    private val commands = connection.coroutines()

    @EventListener(ApplicationReadyEvent::class)
    suspend fun setup() {
        init("BF:news")
        init("BF:disclosure")
        init("BF:paper")
        init("BF:social")
    }

    suspend fun init(
            filterName: String,
            errorRate: Double = 0.01,
            capacity: Long = 300_000
    ) {
        val args = CommandArgs(StringCodec.UTF8)
                .add(filterName)
                .add(errorRate.toString())
                .add(capacity.toString())

        withContext(Dispatchers.IO) {
            try {
                commands.dispatch(
                        BloomCommand.RESERVE,
                        StatusOutput(StringCodec.UTF8),
                        args
                ).single()   // Flow<String> → String
            } catch (_: Exception) {
                // 이미 생성된 경우 무시
            }
        }
    }

    suspend fun exists(filterName: String, key: String): Boolean {
        val args = CommandArgs(StringCodec.UTF8)
                .add(filterName)
                .add(key)

        val result = withContext(Dispatchers.IO) {
            commands.dispatch(
                    BloomCommand.EXISTS,
                    IntegerOutput(StringCodec.UTF8),
                    args
            ).single()  // Flow<Long> → Long
        }

        return result == 1L
    }

    suspend fun add(filterName: String, key: String) {
        val args = CommandArgs(StringCodec.UTF8)
                .add(filterName)
                .add(key)

        withContext(Dispatchers.IO) {
            commands.dispatch(
                    BloomCommand.ADD,
                    IntegerOutput(StringCodec.UTF8),
                    args
            ).single()
        }
    }
}
