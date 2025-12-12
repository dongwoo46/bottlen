package com.bottlen.bottlen_webflux.news.client.api

import com.bottlen.bottlen_webflux.news.dto.api.NewsDto
import com.bottlen.bottlen_webflux.news.domain.NewsSource
import com.bottlen.bottlen_webflux.news.dto.api.NewsDataResponseDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.net.URLEncoder

/**
 * ✅ NewsData.io Latest News API Client (Free Tier 대응)
 * - https://newsdata.io/api/1/latest
 * - q 파라미터 제거 (category + domainurl 조합만 사용)
 * - nextPage 기반 전체 페이지(fetchAll) 수집
 */
@Component
class NewsDataClient(
        webClientBuilder: WebClient.Builder,
        @Value("\${external.news.newsdata.base-url}") private val baseUrl: String,
        @Value("\${external.news.newsdata.api-key}") private val apiKey: String
) : NewsClient {

    override val sourceName: String = NewsSource.NEWS_DATA.name
    override val supportsMultipleSources = true

    private val webClient: WebClient = webClientBuilder
            .baseUrl(baseUrl)
            .build()

    /**
     * ✅ 도메인 + 카테고리 단위 전체 뉴스 수집
     * 도메인별 → 카테고리별 → 기사리스트 형태로 묶어서 반환
     */
    override fun fetchByDomainAndCategory(
            domains: List<String>,
            categories: List<String>
    ): Flux<Pair<String, Map<String, List<NewsDto>>>> {

        return Flux.fromIterable(domains)
                .flatMap { domain ->
                    Flux.fromIterable(categories)
                            .flatMap { category ->
                                fetchAll(domain, category)
                                        .collectList()
                                        .map { category to it }
                            }
                            .collectMap({ it.first }, { it.second }) // Map<category, List<NewsDto>>
                            .map { domain to it } // Pair(domain, Map<category, List<NewsDto>>)
                }
    }

    /**
     * ✅ 단일 domain + category 단위로 전체 페이지 탐색(fetchAll)
     */
    private fun fetchAll(
            domain: String,
            category: String,
            pageToken: String? = null
    ): Flux<NewsDto> {
        return webClient.get()
                .uri { b ->
                    b.path("/latest")
                            .queryParam("apikey", apiKey)
                            .queryParam("language", "en")
                            .queryParam("size", 10)
                            .queryParam("removeduplicate", 1)
                            .queryParam("domainurl", domain)
                            .queryParam("category", category)
                            .apply {
                                if (!pageToken.isNullOrBlank())
                                    queryParam("page", pageToken)
                            }
                            .build()
                }
                .retrieve()
                .bodyToMono(NewsDataResponseDto::class.java)
                .flatMapMany { response ->
                    val current = Flux.fromIterable(response.toNewsDtoList(category))

                    // ✅ 다음 페이지가 존재하면 재귀적으로 fetchAll 호출
                    if (!response.nextPage.isNullOrBlank()) {
                        current.concatWith(fetchAll(domain, category, response.nextPage))
                    } else {
                        current
                    }
                }
                .onErrorResume { e ->
                    val debugUrl = buildDebugUrl(domain, category, pageToken)
                    println("[NewsDataClient] ❌ ${domain}(${category}) API 실패: ${e.message}")
                    println("   ↳ 요청 URL: $debugUrl")
                    Flux.empty()
                }
    }

    /**
     * ✅ 디버그용 URL 생성기
     */
    private fun buildDebugUrl(domain: String, category: String, pageToken: String?): String {
        val builder = StringBuilder(baseUrl.trimEnd('/'))
                .append("/latest?apikey=$apiKey&language=en&size=50&removeduplicate=1")
                .append("&domainurl=${URLEncoder.encode(domain, Charsets.UTF_8)}")
                .append("&category=${URLEncoder.encode(category, Charsets.UTF_8)}")

        if (!pageToken.isNullOrBlank()) {
            builder.append("&page=${URLEncoder.encode(pageToken, Charsets.UTF_8)}")
        }

        return builder.toString()
    }
}
