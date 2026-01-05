package com.bottlen.bottlen_webflux.news.client.rss

import reactor.core.publisher.Flux
import com.bottlen.bottlen_webflux.news.dto.rss.RssArticle
import com.bottlen.bottlen_webflux.news.dto.rss.RssFeedConfig

interface RssClient {

    fun supportedSource(): String

    /**
     * 하나의 RSS Feed 설정을 받아
     * - XML fetch
     * - 파싱
     * - RssArticle 변환
     */
    fun fetchArticles(feed: RssFeedConfig): Flux<RssArticle>
}
