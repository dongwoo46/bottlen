package com.bottlen.bottlen_webflux.news.client.api

import com.bottlen.bottlen_webflux.news.dto.api.GdeltResponseDto
import com.bottlen.bottlen_webflux.news.dto.api.NewsDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Component
class GdeltClient(
        webClientBuilder: WebClient.Builder,
        @Value("\${external.news.gdelt.doc-base-url}") private val docBaseUrl: String,
        @Value("\${external.news.gdelt.context-base-url}") private val contextBaseUrl: String
) {

    /**
     * DOC API 호출 전용 WebClient
     */
    private val docClient: WebClient = webClientBuilder
            .baseUrl(docBaseUrl)
            .build()

    /**
     * CONTEXT API 호출 전용 WebClient
     */
    private val contextClient: WebClient = webClientBuilder
            .baseUrl(contextBaseUrl)
            .build()

    /**
     * ✅ DOC API: 기사 데이터 수집
     */
    fun fetchDoc(
            query: String,
            domains: List<String>? = null,
            category: String = "general",
            timespan: String = "7d",
            maxRecords: Int = 100
    ): Flux<NewsDto> {
        val domainFilter = domains?.joinToString(" OR ") { "domain:$it" }
        val finalQuery = if (domainFilter != null) "$query ($domainFilter)" else query

        return docClient.get()
                .uri { builder ->
                    builder
                            .queryParam("query", finalQuery)
                            .queryParam("format", "json")
                            .queryParam("maxrecords", maxRecords)
                            .queryParam("timespan", timespan)
                            .build()
                }
                .retrieve()
                .bodyToMono(GdeltResponseDto::class.java)
                .flatMapMany { Flux.fromIterable(it.toNewsDtoList(category)) }
    }

    /**
     * ✅ CONTEXT API: 키워드 언급량 시계열 분석
     */
    fun fetchContext(
            query: String,
            mode: String = "TimelineVolume",
            timespan: String = "30d"
    ): Flux<Map<String, Any>> {
        return contextClient.get()
                .uri { builder ->
                    builder
                            .queryParam("query", query)
                            .queryParam("format", "json")
                            .queryParam("mode", mode)
                            .queryParam("timespan", timespan)
                            .build()
                }
                .retrieve()
                .bodyToMono(Map::class.java)
                .flatMapMany { response ->
                    val series = (response["timeline"] as? Map<*, *>)?.get("default") as? Map<*, *>
                    val data = series?.get("series") as? List<Map<String, Any>> ?: emptyList()
                    Flux.fromIterable(data)
                }
    }
}
