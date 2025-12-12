package com.bottlen.bottlen_webflux.news.client.api

import com.bottlen.bottlen_webflux.news.dto.api.NewsDto
import com.bottlen.bottlen_webflux.news.domain.NewsSource
import com.bottlen.bottlen_webflux.news.dto.api.NewYorkTimesResponseDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

/**
 * New York Times Top Stories API Client
 * 예시: https://api.nytimes.com/svc/topstories/v2/{section}.json
 */
@Component
class NewYorkTimesClient(
        webClientBuilder: WebClient.Builder,
        @Value("\${external.news.newyorktimes.base-url}") private val baseUrl: String,
        @Value("\${external.news.newyorktimes.api-key}") private val apiKey: String
) : NewsClient {

    override val sourceName: String = NewsSource.NYT.name
    override val supportsMultipleSources = false

    private val webClient: WebClient = webClientBuilder
            .baseUrl(baseUrl)
            .build()

    /**
     * NYT는 section 단위로 뉴스 제공 (e.g., business, world, technology)
     * 여러 section 병렬 호출 → Flux 병합
     */
    override fun fetch(categories: List<String>): Flux<NewsDto> {
        val requests = categories.map { section ->
            webClient.get()
                    .uri {
                        it.path("/$section.json")
                                .queryParam("api-key", apiKey)
                                .build()
                    }
                    .retrieve()
                    .bodyToMono(NewYorkTimesResponseDto::class.java)
                    .flatMapMany { response ->
                        Flux.fromIterable(response.toNewsDtoList(section))
                    }
                    .onErrorResume { e ->
                        println("[NewYorkTimesClient] ${section} 섹션 호출 실패: ${e.message}")
                        Flux.empty()
                    }
        }

        return Flux.merge(requests)
    }
}
