package com.bottlen.bottlen_webflux.social.service

import com.bottlen.bottlen_webflux.social.client.RedditClient
import com.bottlen.bottlen_webflux.social.dto.SocialDto
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class RedditService(
        private val redditClient: RedditClient
) : SocialService {

    override fun fetch(source: String, limit: Int): Flux<SocialDto> {
        return redditClient.fetchSubreddit(source, limit)
                .flatMapMany { Flux.fromIterable(it.toSocialDtoList()) }
    }
}
