package com.bottlen.bottlen_webflux.paper.client

import com.bottlen.bottlen_webflux.infra.config.ExternalProperties
import com.bottlen.bottlen_webflux.paper.dto.opencitations.OpenCitationsResponse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.net.URI

@Component
class OpenCitationsClient(
    webClientBuilder: WebClient.Builder,
    externalProperties: ExternalProperties
) : CitationClient {

    private val log = LoggerFactory.getLogger(javaClass)

    private val baseUrl = externalProperties.paper.opencitations.baseUrl

    private val webClient: WebClient = webClientBuilder
        .baseUrl(baseUrl)
        .build()

    override suspend fun fetchCitations(doi: String): List<OpenCitationsResponse> {
        return webClient.get()
            .uri { builder -> buildCitationsUri(doi, builder) }
            .retrieve()
            .onStatus({ it.is4xxClientError }) { response ->
                response.bodyToMono(String::class.java)
                    .doOnNext {
                        log.warn(
                            "OpenCitations client error (citations): doi={}, status={}, body={}",
                            doi,
                            response.statusCode(),
                            it
                        )
                    }
                    .then(Mono.empty())
            }
            .onStatus({ it.is5xxServerError }) { response ->
                response.bodyToMono(String::class.java)
                    .flatMap {
                        log.error(
                            "OpenCitations server error (citations): doi={}, status={}, body={}",
                            doi,
                            response.statusCode(),
                            it
                        )
                        Mono.error(RuntimeException("OpenCitations server error"))
                    }
            }
            .bodyToFlux(OpenCitationsResponse::class.java)
            .collectList()
            .awaitSingleOrNull()
            ?: emptyList()
    }

    override suspend fun fetchReferences(doi: String): List<OpenCitationsResponse> {
        return webClient.get()
            .uri { builder -> buildReferencesUri(doi, builder) }
            .retrieve()
            .onStatus({ it.is4xxClientError }) { response ->
                response.bodyToMono(String::class.java)
                    .doOnNext {
                        log.warn(
                            "OpenCitations client error (references): doi={}, status={}, body={}",
                            doi,
                            response.statusCode(),
                            it
                        )
                    }
                    .then(Mono.empty())
            }
            .onStatus({ it.is5xxServerError }) { response ->
                response.bodyToMono(String::class.java)
                    .flatMap {
                        log.error(
                            "OpenCitations server error (references): doi={}, status={}, body={}",
                            doi,
                            response.statusCode(),
                            it
                        )
                        Mono.error(RuntimeException("OpenCitations server error"))
                    }
            }
            .bodyToFlux(OpenCitationsResponse::class.java)
            .collectList()
            .awaitSingleOrNull()
            ?: emptyList()
    }

    /**
     * 🔹 인용(Citations) URI 생성
     */
    private fun buildCitationsUri(
        doi: String,
        uriBuilder: UriBuilder
    ): URI =
        uriBuilder
            .path("/citations/{doi}")
            .build(doi.lowercase())

    /**
     * 🔹 참고문헌(References) URI 생성
     */
    private fun buildReferencesUri(
        doi: String,
        uriBuilder: UriBuilder
    ): URI =
        uriBuilder
            .path("/references/{doi}")
            .build(doi.lowercase())
}
