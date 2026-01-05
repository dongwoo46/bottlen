package com.bottlen.bottlen_webflux.news.client.rss

import com.bottlen.bottlen_webflux.news.dto.rss.RssArticle
import com.bottlen.bottlen_webflux.news.dto.rss.RssFeedConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Instant

@Component
class EconomistClient(
    webClient: WebClient
) : AbstractRssClient(webClient) {

    override fun supportedSource(): String = "economist"

    override fun parseInternal(
        xml: String,
        feed: RssFeedConfig
    ): Flux<RssArticle> {

        // Economist RSS는 전형적인 item 구조
        return Mono.fromCallable {
            Jsoup.parse(xml, "", Parser.xmlParser())
                .select("item")
        }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany { items ->
                Flux.fromIterable(items)
                    .mapNotNull { item ->
                        parseItem(item, feed)
                    }
            }
    }

    /**
     * Economist RSS item → RssArticle 변환
     */
    private fun parseItem(
        item: Element,
        feed: RssFeedConfig
    ): RssArticle? {

        val title = item.selectFirst("title")
            ?.text()
            ?.trim()
            .orEmpty()

        val link = item.selectFirst("link")
            ?.text()
            ?.trim()
            .orEmpty()

        if (title.isBlank() || link.isBlank()) return null

        val summary = RssParseUtils.cleanHtml(
            item.selectFirst("description")
                ?.html()
                .orEmpty()
        )

        val content = RssParseUtils.cleanHtml(
            extractContent(item)
        )

        val published = RssParseUtils.normalizeDate(
            item.selectFirst("pubDate")
                ?.text()
                .orEmpty()
        )

        val author = item
            .selectFirst("author, dc\\:creator")
            ?.text()
            ?.trim()

        return RssArticle(
            source = feed.source,     // "economist"
            topic = feed.topic,       // main / finance / geopolitics ...
            title = title,
            link = link,
            summary = summary,
            content = content,
            published = published,
            author = author,
            lang = "en",
            collectedAt = Instant.now().toString()
        )
    }

    /**
     * Economist 본문 추출
     *
     * Python 대응:
     * - entry.content[0].value
     * - 없으면 summary 기반
     */
    private fun extractContent(item: Element): String {

        // content:encoded 우선
        item.selectFirst("content\\:encoded")
            ?.html()
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        // description fallback
        item.selectFirst("description")
            ?.html()
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        return ""
    }
}
