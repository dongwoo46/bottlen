package com.bottlen.bottlen_webflux.paper.client

import com.bottlen.bottlen_webflux.infra.config.ExternalProperties
import com.bottlen.bottlen_webflux.paper.domain.Paper
import com.bottlen.bottlen_webflux.paper.dto.FetchRequest
import com.bottlen.bottlen_webflux.paper.dto.FetchResponse
import com.bottlen.bottlen_webflux.paper.dto.crossref.CrossrefResponse
import com.bottlen.bottlen_webflux.paper.dto.crossref.CrossrefSingleResponse
import com.bottlen.bottlen_webflux.paper.mapper.toPaper
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class CrossrefClient(
    webClientBuilder: WebClient.Builder,
    externalProperties: ExternalProperties
) : PaperClient {

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
        val uri = buildUri(request)

        val response = webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(CrossrefResponse::class.java)
            .awaitSingle()

        val items = response.message?.items ?: emptyList()
        val papers = items.mapNotNull { it.toPaper() }

        return FetchResponse(
            papers = papers,
            nextCursor = response.message?.nextCursor
        )
    }

    override suspend fun fetchByDoi(doi: String): Paper? {
        val encodedDoi = encode(doi)

        val response = webClient.get()
            .uri("/works/$encodedDoi")
            .retrieve()
            .bodyToMono(CrossrefSingleResponse::class.java)
            .awaitSingle()

        return response.message?.toPaper()
    }

    private fun buildUri(req: FetchRequest): String {
        val params = mutableListOf<String>()
        val filters = mutableListOf<String>()

        // 🔹 검색어
        req.keyword?.let {
            params += "query=${encode(it)}"
        }

        // 🔹 날짜 필터 (하나의 filter로 합침)
        req.fromDate?.let { filters += "from-pub-date:$it" }
        req.toDate?.let { filters += "until-pub-date:$it" }

        if (filters.isNotEmpty()) {
            params += "filter=${filters.joinToString(",")}"
        }

        // 🔹 정렬
        req.sort?.let {
            params += "sort=$it"
        }

        // 🔹 페이징
        params += "rows=${req.limit}"
        params += "cursor=${req.cursor ?: "*"}"

        return "/works?${params.joinToString("&")}"
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8)
}
