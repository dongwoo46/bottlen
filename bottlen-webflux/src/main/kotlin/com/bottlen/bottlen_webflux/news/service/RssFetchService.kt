package com.bottlen.bottlen_webflux.news.service

import com.bottlen.bottlen_webflux.news.client.rss.RssClient
import com.bottlen.bottlen_webflux.news.dto.rss.RssArticle
import com.bottlen.bottlen_webflux.news.dto.rss.RssFeedConfig
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class RssFetchService(
    private val rssClients: List<RssClient>
) {
    private val clientMap: Map<String, RssClient> =
        rssClients.associateBy { it.supportedSource() }

    fun fetch(feed: RssFeedConfig): Flux<RssArticle> {
        val client = clientMap[feed.source]
            ?: error("Unsupported RSS source: ${feed.source}")
        return client.fetchArticles(feed)
    }
}
