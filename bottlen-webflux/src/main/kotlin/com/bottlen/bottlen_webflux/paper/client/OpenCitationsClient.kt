package com.bottlen.bottlen_webflux.paper.client

import com.bottlen.bottlen_webflux.infra.config.ExternalProperties
import com.bottlen.bottlen_webflux.paper.dto.opencitations.OpenCitationsResponse
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class OpenCitationsClient(
    webClientBuilder: WebClient.Builder,
    externalProperties: ExternalProperties
) : CitationClient {

    private val baseUrl = externalProperties.paper.opencitations.baseUrl

    private val webClient: WebClient = webClientBuilder
        .baseUrl(baseUrl)
        .build()

    override suspend fun fetchCitations(doi: String): List<OpenCitationsResponse> {
        val encodedDoi = encode(doi)

        return webClient.get()
            .uri("/citations/$encodedDoi")
            .retrieve()
            .bodyToFlux(OpenCitationsResponse::class.java)
            .collectList()
            .awaitSingle()
    }

    override suspend fun fetchReferences(doi: String): List<OpenCitationsResponse> {
        val encodedDoi = encode(doi)

        return webClient.get()
            .uri("/references/$encodedDoi")
            .retrieve()
            .bodyToFlux(OpenCitationsResponse::class.java)
            .collectList()
            .awaitSingle()
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8)
}
