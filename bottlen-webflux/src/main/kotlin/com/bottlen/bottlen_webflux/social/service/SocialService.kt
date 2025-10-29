package com.bottlen.bottlen_webflux.social.service

import com.bottlen.bottlen_webflux.social.dto.SocialDto
import reactor.core.publisher.Flux

/**
 * 모든 소셜 플랫폼 서비스가 따라야 하는 공통 인터페이스.
 * RedditService, TelegramService, DiscordService 등에서 구현.
 */
interface SocialService {

    /**
     * 지정된 소스(subreddit, channel, group 등)에서 데이터를 가져온다.
     *
     * @param source 플랫폼별 소스 이름
     * @param limit 가져올 데이터 개수 (기본 10)
     */
    fun fetch(source: String, limit: Int = 10): Flux<SocialDto>
}
