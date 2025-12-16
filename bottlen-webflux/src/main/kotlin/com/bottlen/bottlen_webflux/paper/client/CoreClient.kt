package com.bottlen.bottlen_webflux.paper.client

import com.bottlen.bottlen_webflux.infra.config.ExternalProperties
import com.bottlen.bottlen_webflux.paper.domain.PaperDocument
import com.bottlen.bottlen_webflux.paper.dto.core.CoreResponse
import com.bottlen.bottlen_webflux.paper.mapper.toPaperDocument
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
@Component
class CoreClient(
    webClientBuilder: WebClient.Builder,
    externalProperties: ExternalProperties
) : PaperFullTextClient {

    private val log = LoggerFactory.getLogger(javaClass)

    private val core = externalProperties.paper.core

    private val webClient: WebClient = webClientBuilder
        .baseUrl(core.baseUrl)
        .defaultHeader("Authorization", "Bearer ${core.apiKey}")
        .build()

    override suspend fun fetchByDoi(doi: String): PaperDocument? {
        return try {
            val response = webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/search/works")
                        .queryParam("q", doi)
                        .queryParam("limit", 1)
                        .build()
                }
                .retrieve()
                .onStatus({ it.is4xxClientError }) { response ->
                    response.bodyToMono(String::class.java)
                        .doOnNext {
                            log.warn(
                                "CORE client error: doi={}, status={}, body={}",
                                doi,
                                response.statusCode(),
                                it
                            )
                        }
                        // 논문 없음 / 잘못된 요청 → 정상 흐름
                        .then(Mono.empty())
                }
                .onStatus({ it.is5xxServerError }) { response ->
                    response.bodyToMono(String::class.java)
                        .flatMap {
                            log.error(
                                "CORE server error: doi={}, status={}, body={}",
                                doi,
                                response.statusCode(),
                                it
                            )
                            Mono.error(RuntimeException("CORE server error"))
                        }
                }
                .bodyToMono(CoreResponse::class.java)
                .awaitSingleOrNull()

            response?.results
                ?.firstOrNull()
                ?.toPaperDocument()

        } catch (e: Exception) {
            // 진짜 예상 못한 오류만 여기로 옴
            log.error("CORE unexpected error: doi={}", doi, e)
            null
        }
    }
}
