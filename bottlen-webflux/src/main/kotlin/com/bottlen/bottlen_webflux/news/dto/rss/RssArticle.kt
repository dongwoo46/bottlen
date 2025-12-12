package com.bottlen.bottlen_webflux.news.dto.rss


data class RssArticle(
        val id: String,             // 해시 (link + title)
        val source: String,         // cnbc / reuters / physorg ...
        val topic: String,          // us / business / science ...
        val title: String,
        val link: String,
        val summary: String,
        val content: String,
        val published: String,      // ISO 8601 변환된 발행 시간
        val author: String?,        // 없을 수 있음
        val lang: String?,          // en / ko / etc (감지된 언어)
        val collectedAt: String     // ISO 8601 수집 시간
)