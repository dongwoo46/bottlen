package com.bottlen.bottlen_webflux.news.client.rss

import com.bottlen.bottlen_webflux.news.dto.rss.RssArticle
import com.bottlen.bottlen_webflux.news.dto.rss.RssFeedConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.Instant

@Component
class ArsTechnicaClient(
    webClient: WebClient
) : AbstractRssClient(webClient) {

    override fun supportedSource(): String = "ars_technica"
    private val log = LoggerFactory.getLogger(javaClass)


    override fun parseInternal(
        xml: String,
        feed: RssFeedConfig
    ): Flux<RssArticle> {

        // 1. 입력 검증: 빈 XML 방어
        if (xml.isBlank()) {
            log.warn("[RSS][PARSE] empty xml. source={}", feed.source)
            return Flux.empty()
        }

        // 2. XML 크기 제한 (OOM 방지)
        val xmlSizeBytes = xml.length * 2
        val maxSize = 10 * 1024 * 1024
        if (xmlSizeBytes > maxSize) {
            log.error(
                "[RSS][PARSE] xml too large. source={}, size=~{} bytes",
                feed.source,
                xmlSizeBytes
            )
            return Flux.empty()
        }

        return Mono.fromCallable {
            log.debug(
                "[RSS][PARSE] start parsing. source={}, size=~{} bytes",
                feed.source,
                xmlSizeBytes
            )

            Jsoup.parse(xml, "", Parser.xmlParser())
                .select("item, entry")
        }
            // 3️. 전체 XML 파싱 타임아웃 (boundedElastic 보호)
            .timeout(Duration.ofSeconds(30))
            .subscribeOn(Schedulers.boundedElastic())
            // 4️. XML 파싱 실패 시 feed 단위로 안전 종료
            .onErrorResume { e ->
                log.error(
                    "[RSS][PARSE] xml parse failed. source={}",
                    feed.source,
                    e
                )
                Mono.empty()
            }
            .flatMapMany { entries ->
                Flux.fromIterable(entries)
                    .flatMap { entry ->
                        Mono.fromCallable { parseEntry(entry, feed) }
                            // 5️⃣ entry 단위 에러 보호
                            .onErrorResume { e ->
                                log.warn(
                                    "[RSS][PARSE] entry parse failed. source={}",
                                    feed.source,
                                    e
                                )
                                Mono.empty()
                            }
                            // 6️⃣ null 처리 + 로깅을 한 번에
                            .flatMap { article ->
                                if (article == null) {
                                    log.debug(
                                        "[RSS][PARSE] entry dropped. source={}",
                                        feed.source
                                    )
                                    Mono.empty()
                                } else {
                                    Mono.just(article)
                                }
                            }
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
