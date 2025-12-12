package com.bottlen.bottlen_webflux.news.service

import com.bottlen.bottlen_webflux.news.domain.NewsSource
import com.bottlen.bottlen_webflux.news.client.api.NewsClient
import com.bottlen.bottlen_webflux.news.dto.api.NewsDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service

@Service
class NewsService(
        private val clients: List<NewsClient>
) {

    /**
     * 단일 소스(Guardian, NYT 등) 뉴스 조회
     */
    suspend fun fetchNews(
            categories: List<String>,
            source: NewsSource
    ): List<NewsDto> = coroutineScope {
        val targetClients = clients.filter {
            it.sourceName.equals(source.name, ignoreCase = true) &&
                    !it.supportsMultipleSources
        }

        val results = targetClients.map { client ->
            async {
                try {
                    client.fetch(categories)
                            .collectList()
                            .awaitSingleOrNull() ?: emptyList()
                } catch (e: Exception) {
                    println("⚠️ [${client.sourceName}] fetch 실패: ${e.message}")
                    emptyList()
                }
            }
        }.awaitAll()

        results.flatten()
    }

    /**
     * ✅ 멀티 소스용: 도메인 + 카테고리 단위 뉴스 조회 (NewsData.io 등)
     * 예: reuters.com, businesswire.com 각각에서 world, technology 뉴스 수집
     *
     * @param domains     조회할 뉴스 도메인 리스트
     * @param categories  조회할 카테고리 리스트
     * @param source      사용할 API 소스 (예: NEWS_DATA)
     * @return Map<domain, Map<category, List<NewsDto>>>
     */
    suspend fun fetchByDomainAndCategoryNews(
            domains: List<String>,
            categories: List<String>,
            source: NewsSource
    ): Map<String, Map<String, List<NewsDto>>> = coroutineScope {
        val targetClients = clients.filter {
            it.sourceName.equals(source.name, ignoreCase = true) &&
                    it.supportsMultipleSources
        }

        val results = targetClients.map { client ->
            async {
                try {
                    client.fetchByDomainAndCategory(domains, categories)
                            .collectList()
                            .awaitSingleOrNull()
                            ?.groupBy({ it.first }, { it.second }) // domain별로 묶기
                            ?.mapValues { (_, list) ->
                                // category map 병합
                                list.flatMap { it.entries }
                                        .groupBy({ it.key }, { it.value })
                                        .mapValues { (_, lists) -> lists.flatten() }
                            }
                            ?: emptyMap()
                } catch (e: Exception) {
                    println("⚠️ [${client.sourceName}] fetchByDomainAndCategory 실패: ${e.message}")
                    emptyMap()
                }
            }
        }.awaitAll()

        mergeDomainCategoryResults(results)
    }

    /**
     * ✅ 공통 Map 병합 로직 (domain + category 단위)
     * @param domains     조회할 뉴스 도메인 리스트
     * @param categories  조회할 카테고리 리스트
     */
    private fun mergeDomainCategoryResults(
            results: List<Map<String, Map<String, List<NewsDto>>>>
    ): Map<String, Map<String, List<NewsDto>>> {
        return results
                .flatMap { it.entries } // domain-level entries
                .groupBy({ it.key }, { it.value })
                .mapValues { (_, categoryMaps) ->
                    categoryMaps
                            .flatMap { it.entries }
                            .groupBy({ it.key }, { it.value })
                            .mapValues { (_, lists) -> lists.flatten() }
                }
    }
}
