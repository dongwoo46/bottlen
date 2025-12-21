package com.bottlen.bottlen_webflux.news.dto.rss

import com.bottlen.bottlen_webflux.news.domain.Topic


data class RssArticle(
        val source: String,         // cnbc / reuters / physorg ...
        val topic: Topic,          // us / business / science ...
        val title: String,
        val link: String,
        val summary: String,
        val content: String,
        val published: String,      // ISO 8601 변환된 발행 시간
        val author: String?,        // 없을 수 있음
        val lang: String?,          // en / ko / etc (감지된 언어)
        val collectedAt: String     // ISO 8601 수집 시간
)