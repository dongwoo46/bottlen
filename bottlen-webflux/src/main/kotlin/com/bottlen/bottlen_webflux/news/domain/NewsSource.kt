package com.bottlen.bottlen_webflux.news.domain

/**
 * 뉴스 데이터 출처를 명확하게 구분하기 위한 Enum
 * - 외부 API/공급자 단위로 구분
 * - displayName은 가독성용 (로그, 대시보드 표시용)
 */
enum class NewsSource(val displayName: String) {

    // ──────────────── 기본 API 소스 ────────────────
    GUARDIAN("The Guardian"),
    NYT("New York Times"),
    NEWS_DATA("NewsData.io"),
    NEWS_CATCHER("NewsCatcher"),

    // ──────────────── 추가 확장 소스 ────────────────
    FORTUNE("Fortune"),
    BLOOMBERG("Bloomberg"),
    REUTERS("Reuters"),
    BUSINESS_WIRE("BusinessWire"),
    CIO("CIO"),
    CRUNCHBASE("Crunchbase"),
    WSJ("Wall Street Journal"),
    YAHOO_FINANCE("Yahoo Finance"),
    FORBES("Forbes");

    companion object {
        fun from(value: String): NewsSource =
                entries.find { it.name.equals(value, ignoreCase = true) }
                        ?: throw IllegalArgumentException("지원하지 않는 뉴스 소스입니다: $value")
    }
}
