package com.bottlen.bottlen_webflux.news.client

import com.bottlen.news.client.NewsClient
import com.bottlen.news.dto.NewsDto
import com.bottlen.bottlen_webflux.news.domain.NewsSource
import com.bottlen.bottlen_webflux.news.dto.GuardianResponseDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Component
class GuardianClient(
        private val webClientBuilder: WebClient.Builder,
        @Value("\${external.news.guardian.base-url}") private val baseUrl: String,
        @Value("\${external.news.guardian.api-key}") private val apiKey: String
) : NewsClient {

    override val sourceName: String = NewsSource.GUARDIAN.name
    override val supportsMultipleSources = false


    // ✅ baseUrl 미리 세팅해서 재사용 가능한 WebClient 구성
    private val webClient: WebClient = webClientBuilder
            .baseUrl(baseUrl)
            .build()

    /**
     * Guardian API는 section(category)별로 뉴스를 조회한다.
     * 여러 section을 병렬 호출하여 Flux 병합
     */
    override fun fetch(categories: List<String>): Flux<NewsDto> {
        val requests = categories.map { category ->
            webClient.get()
                    .uri { uriBuilder ->
                        uriBuilder.path("/search")
                                .queryParam("section", category)
                                .queryParam("show-fields", "trailText")
                                .queryParam("api-key", apiKey)
                                .build()
                    }
                    .retrieve()
                    .bodyToMono(GuardianResponseDto::class.java)
                    .flatMapMany { Flux.fromIterable(it.toNewsDtoList(category)) }
        }

        return Flux.merge(requests)
    }
}
