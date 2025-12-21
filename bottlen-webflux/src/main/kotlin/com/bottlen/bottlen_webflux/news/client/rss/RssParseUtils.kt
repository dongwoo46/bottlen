package com.bottlen.bottlen_webflux.news.client.rss

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * RSS / Atom 파싱 시 공통으로 사용되는 유틸리티
 *
 * - 뉴스사와 무관한 처리만 포함한다
 * - 상태를 가지지 않는 순수 함수 모음
 */
object RssParseUtils {

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
     * 실패 시 현재 시각 반환
     */
    fun normalizeDate(raw: String): String {
        if (raw.isBlank()) return Instant.now().toString()

        return try {
            DateTimeFormatter.RFC_1123_DATE_TIME
                .parse(raw, Instant::from)
                .toString()
        } catch (_: Exception) {
            Instant.now().toString()
        }
    }

    /**
     * Atom <link href=""> 또는 RSS <link> 텍스트 추출
     */
    fun extractLink(entry: Element): String {
        return entry.selectFirst("link[href]")?.attr("href")
            ?: entry.selectFirst("link")?.text().orEmpty()
    }
}
