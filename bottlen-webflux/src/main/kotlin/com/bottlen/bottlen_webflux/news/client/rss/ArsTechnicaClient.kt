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
class ArsTechnicaClient(
    webClient: WebClient
) : AbstractRssClient(webClient) {

    override fun supportedSource(): String = "ars_technica"

    override fun parseInternal(
        xml: String,
        feed: RssFeedConfig
    ): Flux<RssArticle> {

        /**
         * Jsoup XML 파싱은 blocking + CPU 작업이므로
         * 반드시 boundedElastic에서 수행
         */
        return Mono.fromCallable {
            Jsoup.parse(xml, "", Parser.xmlParser())
                .select("item, entry")
        }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany { entries ->
                Flux.fromIterable(entries)
                    .mapNotNull { entry ->
                        parseEntry(entry, feed)
                    }
            }
    }

    /**
     * Ars Technica RSS entry → RssArticle 변환
     */
    private fun parseEntry(
        entry: Element,
        feed: RssFeedConfig
    ): RssArticle? {

        val title = entry.selectFirst("title")
            ?.text()
            ?.trim()
            .orEmpty()

        val link = RssParseUtils.extractLink(entry)
        if (title.isBlank() || link.isBlank()) return null

        val summary = RssParseUtils.cleanHtml(
            entry.selectFirst("summary, description")
                ?.html()
                .orEmpty()
        )

        val content = RssParseUtils.cleanHtml(
            extractContent(entry)
        )

        val published = RssParseUtils.normalizeDate(
            entry.selectFirst("published, pubDate")
                ?.text()
                .orEmpty()
        )

        val author = entry
            .selectFirst("author, dc\\:creator")
            ?.text()
            ?.trim()

        return RssArticle(
            source = feed.source,
            topic = feed.topic,
            title = title,
            link = link,
            summary = summary,
            content = content,
            published = published,
            author = author,
            lang = null,
            collectedAt = Instant.now().toString()
        )
    }

    /**
     * Ars Technica RSS 전용 본문 추출 로직
     * (source-specific)
     */
    private fun extractContent(entry: Element): String {

        // RSS content:encoded 우선
        entry.selectFirst("content\\:encoded")
            ?.html()
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        // Atom content[type=html]
        val atomContent = entry
            .select("content[type=html]")
            .html()

        if (atomContent.isNotBlank()) return atomContent

        return ""
    }
}
