package com.bottlen.bottlen_webflux.paper.client

import com.bottlen.bottlen_webflux.infra.config.ExternalProperties
import com.bottlen.bottlen_webflux.paper.domain.PaperDocument
import com.bottlen.bottlen_webflux.paper.dto.FetchRequest
import com.bottlen.bottlen_webflux.paper.dto.FetchResponse
import com.bottlen.bottlen_webflux.paper.dto.openalex.OpenAlexResponse
import com.bottlen.bottlen_webflux.paper.mapper.toPaperDocument
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.net.URI

@Component
class OpenAlexClient(
    webClientBuilder: WebClient.Builder,
    externalProperties: ExternalProperties
) : PaperClient {

    private val log = LoggerFactory.getLogger(javaClass)

    private val openalex = externalProperties.paper.openalex
    private val mailto = externalProperties.contact.mailto

    private val webClient: WebClient = webClientBuilder
        .baseUrl(openalex.baseUrl)
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
                            "OpenAlex client error: status={}, body={}",
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
                            "OpenAlex server error: status={}, body={}",
                            response.statusCode(),
                            it
                        )
                        Mono.error(RuntimeException("OpenAlex server error"))
                    }
            }
            .bodyToMono(OpenAlexResponse::class.java)
            .awaitSingleOrNull()
            ?: return FetchResponse(emptyList())

        val documents: List<PaperDocument> =
            response.results
                ?.mapNotNull { it.toPaperDocument() }
                ?: emptyList()

        return FetchResponse(
            paperDocuments = documents,
            nextCursor = response.meta?.nextCursor
        )
    }

    override suspend fun fetchByDoi(doi: String): PaperDocument? {
        val response = webClient.get()
            .uri { builder -> buildDoiUri(doi, builder) }
            .retrieve()
            .onStatus({ it.is4xxClientError }) { response ->
                response.bodyToMono(String::class.java)
                    .doOnNext {
                        log.warn(
                            "OpenAlex DOI not found: doi={}, status={}, body={}",
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
                            "OpenAlex server error: doi={}, status={}, body={}",
                            doi,
                            response.statusCode(),
                            it
                        )
                        Mono.error(RuntimeException("OpenAlex server error"))
                    }
            }
            .bodyToMono(OpenAlexResponse::class.java)
            .awaitSingleOrNull()

        return response
            ?.results
            ?.firstOrNull()
            ?.toPaperDocument()
    }

    /**
     * 검색 URI 조립 책임
     */
    private fun buildSearchUri(
        req: FetchRequest,
        uriBuilder: UriBuilder
    ): URI {
        val filters = mutableListOf<String>()

        req.fromDate?.let { filters += "from_publication_date:$it" }
        req.toDate?.let { filters += "to_publication_date:$it" }
        filters += "has_doi:true"

        uriBuilder.path("/works")

        req.keyword?.let {
            uriBuilder.queryParam("search", it)
        }

        if (filters.isNotEmpty()) {
            uriBuilder.queryParam("filter", filters.joinToString(","))
        }

        req.sort?.let {
            uriBuilder.queryParam("sort", it)
        }

        uriBuilder
            .queryParam("per-page", req.limit)
            .queryParam("cursor", req.cursor ?: "*")

        return uriBuilder.build()
    }

    /**
     * DOI 단건 조회 URI
     */
    private fun buildDoiUri(
        doi: String,
        uriBuilder: UriBuilder
    ): URI =
        uriBuilder
            .path("/works")
            .queryParam("filter", "doi:${doi.lowercase()}")
            .queryParam("per-page", 1)
            .build()
}
