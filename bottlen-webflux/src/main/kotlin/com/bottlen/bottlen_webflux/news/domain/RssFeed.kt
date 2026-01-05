package com.bottlen.bottlen_webflux.news.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

/**
 * RSS 수집 대상(실제 수집 단위)
 */
@Table("rss_feed")
data class RssFeed(
    @Id
    val id: Long? = null,

    // 뉴스 소스 (ex. ars_technica)
    val source: String,

    // 소스 내 카테고리 (ex. main, science)
    val topic: Topic,

    // RSS Feed URL
    val url: String,

    // 수집 주기 (초)
    val intervalSeconds: Int,

    // 수집 활성화 여부
    val enabled: Boolean = true,

    // 마지막 수집 시각
    val lastIngestedAt: Instant? = null,

    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {

    /**
     * 현재 시각 기준 실행 여부 판단
     */
    fun shouldRun(now: Instant): Boolean {
        if (!enabled) return false
        if (lastIngestedAt == null) return true

        return lastIngestedAt
            .plusSeconds(intervalSeconds.toLong())
            .isBefore(now)
    }
}
