package com.bottlen.bottlen_webflux.paper.client

import com.bottlen.bottlen_webflux.infra.config.ExternalProperties
import com.bottlen.bottlen_webflux.paper.domain.Paper
import com.bottlen.bottlen_webflux.paper.dto.FetchRequest
import com.bottlen.bottlen_webflux.paper.dto.FetchResponse
import com.bottlen.bottlen_webflux.paper.dto.openalex.OpenAlexResponse
import com.bottlen.bottlen_webflux.paper.mapper.toPaper
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class OpenAlexClient(
        webClientBuilder: WebClient.Builder,
        externalProperties: ExternalProperties
) : PaperClient {

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
        val uri = buildUri(request)

        val response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(OpenAlexResponse::class.java)
                .awaitSingle()

        val papers = response.results
                ?.mapNotNull { it.toPaper() }
                ?: emptyList()

        return FetchResponse(
                papers = papers,
                nextCursor = response.meta?.nextCursor
        )
    }

    override suspend fun fetchByDoi(doi: String): Paper? {
        val encodedDoi = encode(doi.lowercase())

        val uri = "/works?filter=doi:$encodedDoi&per-page=1"

        val response = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(OpenAlexResponse::class.java)
            .awaitSingle()

        return response.results
            ?.firstOrNull()
            ?.toPaper()
    }

    private fun buildUri(req: FetchRequest): String {
        val params = mutableListOf<String>()
        val filters = mutableListOf<String>()

        // 🔹 키워드 검색
        req.keyword?.let {
            params += "search=${encode(it)}"
        }

        // 🔹 날짜 필터
        req.fromDate?.let { filters += "from_publication_date:$it" }
        req.toDate?.let { filters += "to_publication_date:$it" }

        // 🔹 최소 품질 필터 (너무 broad한 요청 방지)
        filters += "has_doi:true"

        if (filters.isNotEmpty()) {
            params += "filter=${filters.joinToString(",")}"
        }

        // 🔹 정렬
        req.sort?.let {
            params += "sort=$it"
        }

        // 🔹 페이징
        params += "per-page=${req.limit}"
        params += "cursor=${req.cursor ?: "*"}"

        return "/works?${params.joinToString("&")}"
    }

    private fun encode(value: String): String =
            URLEncoder.encode(value, StandardCharsets.UTF_8)
}
