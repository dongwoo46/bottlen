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
class FinancialTimesClient(
    webClient: WebClient
) : AbstractRssClient(webClient) {

    override fun supportedSource(): String = "financial_times"
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

        // 2. 입력 검증: XML 크기 제한 (OOM 방지, 대략적 추정)
        val xmlSizeBytes = xml.length * 2
        val maxSize = 10 * 1024 * 1024 // 10MB
        if (xmlSizeBytes > maxSize) {
            log.error(
                "[RSS][PARSE] xml too large. source={}, size=~{} bytes",
                feed.source,
                xmlSizeBytes
            )
            return Flux.empty()
        }

        // 3. 전체 XML 파싱 (blocking → boundedElastic)
        return Mono.fromCallable {
            Jsoup.parse(xml, "", Parser.xmlParser())
                .select("item")
        }
            // 4. 파싱 타임아웃 (boundedElastic 보호)
            .timeout(Duration.ofSeconds(30))
            .subscribeOn(Schedulers.boundedElastic())

            // 5. XML 파싱 실패 시 feed 단위로 안전 종료
            .onErrorResume { e ->
                log.error(
                    "[RSS][PARSE] xml parse failed. source={}",
                    feed.source,
                    e
                )
                Mono.empty()
            }

            // 6. entry 단위로 Flux 변환
            .flatMapMany { items ->
                Flux.fromIterable(items)
                    .flatMap { item ->
                        Mono.fromCallable { parseEntry(item, feed) }

                            // 7. entry 파싱 실패는 개별 드랍
                            .onErrorResume { e ->
                                log.warn(
                                    "[RSS][PARSE] entry parse failed. source={}",
                                    feed.source,
                                    e
                                )
                                Mono.empty()
                            }

                            // 8. null entry 가시성 확보
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
     * Financial Times RSS item → RssArticle
     */
    private fun parseEntry(
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
            .selectFirst("dc\\:creator, creator")
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
            lang = "en",
            collectedAt = Instant.now().toString()
        )
    }

    /**
     * Financial Times RSS 본문 추출
     */
    private fun extractContent(item: Element): String {

        // content:encoded (있으면 최우선)
        item.selectFirst("content\\:encoded")
            ?.html()
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        // description fallback
        return item.selectFirst("description")
            ?.html()
            .orEmpty()
    }
}
