package com.bottlen.bottlen_webflux.paper.client

import com.bottlen.bottlen_webflux.infra.config.ExternalProperties
import com.bottlen.bottlen_webflux.paper.dto.core.CoreResponse
import com.bottlen.bottlen_webflux.paper.dto.core.CoreWork
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class CoreClient(
    webClientBuilder: WebClient.Builder,
    externalProperties: ExternalProperties
) : PaperFullTextClient {

    private val core = externalProperties.paper.core

    private val webClient: WebClient = webClientBuilder
        .baseUrl(core.baseUrl)
        .defaultHeader("Authorization", "Bearer ${core.apiKey}")
        .build()

    override suspend fun fetchByDoi(doi: String): CoreWork? {
        val encodedDoi = encode(doi)

        val response = webClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/search/works")
                    .queryParam("q", encodedDoi)
                    .queryParam("limit", 1)
                    .build()
            }
            .retrieve()
            .bodyToMono(CoreResponse::class.java)
            .awaitSingle()

        return response.results?.firstOrNull()
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8)
}
