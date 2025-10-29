package com.bottlen.bottlen_webflux.social.domain

import com.fasterxml.jackson.annotation.JsonCreator

/**
 * 소셜 데이터의 출처(플랫폼) 구분 Enum
 *
 * SocialDto.platform 필드의 문자열 대신, 명확한 타입으로 구분하기 위해 사용한다.
 */
enum class PlatformCategory(val platform: String) {

    /** Reddit - 커뮤니티 기반 뉴스 및 투자자 게시글 */
    REDDIT("reddit"),

    /** Telegram - 채널 또는 그룹 기반 정보 */
    TELEGRAM("telegram"),

    /** Discord - 서버/채널 기반 대화 및 알림 */
    DISCORD("discord");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromPlatform(platform: String): PlatformCategory =
                entries.firstOrNull { it.platform.equals(platform, ignoreCase = true) }
                        ?: throw IllegalArgumentException("Unknown platform: $platform")
    }
}
