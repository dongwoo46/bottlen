package com.bottlen.bottlen_webflux.social.dto

import com.bottlen.bottlen_webflux.social.domain.PlatformCategory
import java.time.Instant

/**
 * Telegram Bot API 응답을 SocialDto 리스트로 변환하는 DTO
 */
data class TelegramResponseDto(
        val ok: Boolean,
        val result: List<TelegramMessage>
) : SocialResponseDto {

    override fun toSocialDtoList(): List<SocialDto> {
        if (!ok) return emptyList()

        return result.mapNotNull { msg ->
            // 텍스트 메시지만 처리
            if (msg.text.isNullOrBlank()) return@mapNotNull null

            SocialDto(
                    platform = PlatformCategory.TELEGRAM,
                    source = msg.chat.username ?: msg.chat.title,
                    sourceId = msg.messageId.toString(),
                    author = msg.from?.username,
                    title = null,
                    content = msg.text.trim(),
                    url = null,
                    createdAt = Instant.ofEpochSecond(msg.date)
            )
        }
    }
}

/** Telegram API JSON 구조 대응 내부 DTO들 */
data class TelegramMessage(
        val messageId: Long,
        val from: TelegramUser?,
        val chat: TelegramChat,
        val date: Long,
        val text: String?
)

data class TelegramUser(val username: String?)
data class TelegramChat(val id: Long, val title: String?, val username: String?)
