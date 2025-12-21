package com.bottlen.bottlen_webflux.news.client.rss

import com.bottlen.bottlen_webflux.news.dto.rss.RssArticle
import com.bottlen.bottlen_webflux.news.dto.rss.RssFeedConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Instant

@Component
class ArsTechnicaClient(
    webClientBuilder: WebClient.Builder
) : AbstractRssClient(webClientBuilder) {

    override fun parseInternal(
        xml: String,
        feed: RssFeedConfig
    ): Flux<RssArticle> {

        return Flux.create<RssArticle> { sink ->
            try {
                val document = Jsoup.parse(
                    xml,
                    "",
                    org.jsoup.parser.Parser.xmlParser()
                )

                val entries = document.select("item, entry")

                for (entry in entries) {
                    val article = parseEntry(entry, feed)
                    if (article != null) {
                        sink.next(article)
                    }
                }

                sink.complete()
            } catch (e: Exception) {
                sink.error(e)
            }
        }
            // ⭐ 핵심: Jsoup 파싱은 이벤트 루프에서 실행되면 안 됨
            .subscribeOn(Schedulers.boundedElastic())
    }

    /**
     * Ars Technica RSS entry 하나를 RssArticle로 변환
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
     * Ars Technica RSS 전용 content 추출 로직
     * (source-specific)
     */
    private fun extractContent(entry: Element): String {
        entry.selectFirst("content\\:encoded")?.html()?.let {
            if (it.isNotBlank()) return it
        }

        val atomContent = entry
            .select("content[type=html]")
            .html()

        if (atomContent.isNotBlank()) return atomContent

        return ""
    }
}
