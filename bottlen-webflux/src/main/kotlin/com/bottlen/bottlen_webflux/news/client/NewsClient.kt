package com.bottlen.news.client

import com.bottlen.news.dto.NewsDto
import reactor.core.publisher.Flux

/**
 * 외부 뉴스 API 호출용 인터페이스
 * 각 구현체는 특정 소스(API 공급자)의 데이터를 호출하고 NewsDto로 변환한다.
 */
interface NewsClient {

    /**
     * ✅ 소스 이름 (예: "Guardian", "NewsData", "Reuters")
     */
    val sourceName: String

    /**
     * ✅ 여러 소스(domainurl) 동시 지원 여부
     */
    val supportsMultipleSources: Boolean get() = false

    /**
     * ✅ 단순 카테고리 단위 기사 수집
     * (예: Guardian의 technology, science 기사)
     */
    fun fetch(categories: List<String>): Flux<NewsDto> = Flux.empty()

    /**
     * ✅ 도메인 + 카테고리 단위 기사 수집
     * @param domains    뉴스 도메인 목록 (예: ["reuters.com", "businesswire.com"])
     * @param categories 카테고리 목록 (예: ["technology", "science"])
     * @return 도메인별 → 카테고리별 → 기사리스트로 그룹화된 Flux
     */
    fun fetchByDomainAndCategory(
            domains: List<String>,
            categories: List<String>
    ): Flux<Pair<String, Map<String, List<NewsDto>>>> = Flux.empty()
}
