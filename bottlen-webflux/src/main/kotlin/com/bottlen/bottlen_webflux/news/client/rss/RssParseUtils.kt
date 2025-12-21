package com.bottlen.bottlen_webflux.news.client.rss

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * RSS / Atom 파싱 시 공통으로 사용되는 유틸리티
 *
 * - 뉴스사와 무관한 처리만 포함한다
 * - 상태를 가지지 않는 순수 함수 모음
 */
object RssParseUtils {

    private val logger = LoggerFactory.getLogger(RssParseUtils::class.java)

    /**
     * HTML 태그 제거 및 공백 정규화
     */
    fun cleanHtml(raw: String): String {
        if (raw.isBlank()) return ""
        return Jsoup.parse(raw).text()
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    /**
     * RSS / Atom 날짜 문자열을 ISO-8601 형식으로 변환
     *
     * 지원 포맷:
     * - RFC 822 / 1123 (RSS)
     * - ISO 8601 / RFC 3339 (Atom)
     *
     * 파싱 실패 시 현재 시각 반환 (fail-soft)
     */
    fun normalizeDate(raw: String): String {
        if (raw.isBlank()) {
            logger.warn("Empty date string, fallback to now")
            return Instant.now().toString()
        }

        for (formatter in DATE_FORMATTERS) {
            try {
                return formatter.parse(raw, Instant::from).toString()
            } catch (_: Exception) {
                // 다음 포맷 시도
            }
        }

        logger.warn(
            "Failed to parse date, fallback to now. raw={}",
            raw
        )
        return Instant.now().toString()
    }

    /**
     * Atom <link href=""> 또는 RSS <link> 텍스트 추출
     */
    fun extractLink(entry: Element): String {
        return entry.selectFirst("link[href]")?.attr("href")
            ?: entry.selectFirst("link")?.text().orEmpty()
    }

    private val DATE_FORMATTERS = listOf(
        DateTimeFormatter.RFC_1123_DATE_TIME,   // RSS
        DateTimeFormatter.ISO_INSTANT,          // Atom (Z)
        DateTimeFormatter.ISO_OFFSET_DATE_TIME  // Atom (+00:00)
    )
}
