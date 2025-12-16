package com.bottlen.bottlen_webflux.paper.client

import com.bottlen.bottlen_webflux.infra.config.ExternalProperties
import com.bottlen.bottlen_webflux.paper.domain.Paper
import com.bottlen.bottlen_webflux.paper.dto.FetchRequest
import com.bottlen.bottlen_webflux.paper.dto.FetchResponse
import com.bottlen.bottlen_webflux.paper.dto.crossref.CrossrefResponse
import com.bottlen.bottlen_webflux.paper.dto.crossref.CrossrefSingleResponse
import com.bottlen.bottlen_webflux.paper.mapper.toPaper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.net.URI

@Component
class CrossrefClient(
    webClientBuilder: WebClient.Builder,
    externalProperties: ExternalProperties
) : PaperClient {

    private val log = LoggerFactory.getLogger(javaClass)

    private val crossref = externalProperties.paper.crossref
    private val mailto = externalProperties.contact.mailto

    private val webClient: WebClient = webClientBuilder
        .baseUrl(crossref.baseUrl)
        .defaultHeader(
            "User-Agent",
            "bottlen/1.0 (mailto:$mailto)"
        )
        .build()

    override suspend fun fetch(request: FetchRequest): FetchResponse {
        val response = webClient.get()
            .uri { builder -> buildSearchUri(request, builder) }
            .retrieve()
            .onStatus({ it.is4xxClientError }) { response ->
                response.bodyToMono(String::class.java)
                    .doOnNext {
                        log.warn(
                            "Crossref client error: status={}, body={}",
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
                            "Crossref server error: status={}, body={}",
                            response.statusCode(),
                            it
                        )
                        Mono.error(RuntimeException("Crossref server error"))
                    }
            }
            .bodyToMono(CrossrefResponse::class.java)
            .awaitSingleOrNull()
            ?: return FetchResponse(emptyList())

        val papers = response.message
            ?.items
            ?.mapNotNull { it.toPaper() }
            ?: emptyList()

        return FetchResponse(
            papers = papers,
            nextCursor = response.message?.nextCursor
        )
    }

    override suspend fun fetchByDoi(doi: String): Paper? {
        val response = webClient.get()
            .uri { builder -> buildDoiUri(doi, builder) }
            .retrieve()
            .onStatus({ it.value() == 404 }) { Mono.empty() }
            .onStatus({ it.is5xxServerError }) { response ->
                response.bodyToMono(String::class.java)
                    .flatMap {
                        log.error(
                            "Crossref server error: status={}, body={}",
                            response.statusCode(),
                            it
                        )
                        Mono.error(RuntimeException("Crossref server error"))
                    }
            }
            .bodyToMono(CrossrefSingleResponse::class.java)
            .awaitSingleOrNull()

        return response?.message?.toPaper()
    }

    /**
     * 🔹 검색 URI 생성 책임 분리
     */
    private fun buildSearchUri(
        request: FetchRequest,
        uriBuilder: UriBuilder
    ): URI {
        val filters = mutableListOf<String>()

        request.fromDate?.let { filters += "from-pub-date:$it" }
        request.toDate?.let { filters += "until-pub-date:$it" }

        uriBuilder.path("/works")

        request.keyword?.let {
            uriBuilder.queryParam("query", it)
        }

        request.sort?.let {
            uriBuilder.queryParam("sort", it)
        }

        if (filters.isNotEmpty()) {
            uriBuilder.queryParam("filter", filters.joinToString(","))
        }

        uriBuilder
            .queryParam("rows", request.limit)
            .queryParam("cursor", request.cursor ?: "*")

        return uriBuilder.build()
    }

    /**
     * 🔹 DOI 단건 조회 URI
     */
    private fun buildDoiUri(
        doi: String,
        uriBuilder: UriBuilder
    ): URI =
        uriBuilder
            .path("/works/{doi}")
            .build(doi)
}
