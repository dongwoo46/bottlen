package com.bottlen.bottlen_webflux.news.client.rss

import com.bottlen.bottlen_webflux.news.dto.rss.RssArticle
import com.bottlen.bottlen_webflux.news.dto.rss.RssFeedConfig
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

abstract class AbstractRssClient(
    webClientBuilder: WebClient.Builder
) : RssClient {

    protected val webClient: WebClient = webClientBuilder.build()

    final override fun fetchArticles(feed: RssFeedConfig): Flux<RssArticle> {
        return webClient.get()
            .uri(feed.rssUrl)
            .retrieve()
            .bodyToMono(String::class.java)
            .flatMapMany { xml ->
                parseInternal(xml, feed)
            }
    }

    protected abstract fun parseInternal(
        xml: String,
        feed: RssFeedConfig
    ): Flux<RssArticle>
}
