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
class CnbcClient(
    webClient: WebClient
) : AbstractRssClient(webClient) {

    override fun supportedSource(): String = "cnbc"
    private val log = LoggerFactory.getLogger(javaClass)

    override fun parseInternal(
        xml: String,
        feed: RssFeedConfig
    ): Flux<RssArticle> {

        // 1. 입력 검증: 빈 XML 방어
        //    - 네트워크 오류, 빈 응답, 상위 fetch 실패 시 빠른 종료
        if (xml.isBlank()) {
            log.warn("[RSS][PARSE] empty xml. source={}", feed.source)
            return Flux.empty()
        }

        // 2. XML 크기 제한 (OOM 방지)
        //    - Jsoup 파싱은 메모리를 한 번에 사용하므로 oversized XML 차단
        val xmlSizeBytes = xml.length * 2   // UTF-16 기준 대략 추정
        val maxSize = 10 * 1024 * 1024      // 10MB
        if (xmlSizeBytes > maxSize) {
            log.error(
                "[RSS][PARSE] xml too large. source={}, size=~{} bytes",
                feed.source,
                xmlSizeBytes
            )
            return Flux.empty()
        }

        // 3. XML 전체 파싱 (blocking 작업)
        //    - Jsoup.parse는 blocking + CPU 작업이므로 Mono.fromCallable로 감싼다
        return Mono.fromCallable {
            log.debug(
                "[RSS][PARSE] start parsing. source={}, size=~{} bytes",
                feed.source,
                xmlSizeBytes
            )

            // CNBC RSS는 표준 RSS 형식 → <item>만 파싱 대상
            Jsoup.parse(xml, "", Parser.xmlParser())
                .select("item")
        }
            // 4. XML 파싱 타임아웃
            //    - malformed XML, 비정상 파싱으로 인한 스레드 고갈 방지
            .timeout(Duration.ofSeconds(30))

            // 5. blocking 작업을 Netty event-loop에서 분리
            //    - boundedElastic 스레드 풀에서 실행
            .subscribeOn(Schedulers.boundedElastic())

            // 6. XML 전체 파싱 실패 시 feed 단위로 안전 종료
            .onErrorResume { e ->
                log.error(
                    "[RSS][PARSE] xml parse failed. source={}",
                    feed.source,
                    e
                )
                Mono.empty()
            }

            // 7. item 단위 Flux로 변환
            .flatMapMany { items ->
                Flux.fromIterable(items)
                    .flatMap { item ->
                        // 8. item 하나를 RssArticle로 변환
                        Mono.fromCallable { parseItem(item, feed) }

                            // 9. item 파싱 실패 보호
                            //    - 해당 item만 drop, 전체 스트림은 유지
                            .onErrorResume { e ->
                                log.warn(
                                    "[RSS][PARSE] item parse failed. source={}",
                                    feed.source,
                                    e
                                )
                                Mono.empty()
                            }

                            // 10. null 결과 처리 + drop 가시성 확보
                            //     - 필수 필드 누락, 유효하지 않은 기사
                            .flatMap { article ->
                                if (article == null) {
                                    log.debug(
                                        "[RSS][PARSE] item dropped. source={}",
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
     * CNBC RSS item → RssArticle 변환
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
            source = feed.source,     // "cnbc"
            topic = feed.topic,       // main / economy / tech ...
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
     * CNBC 본문 추출 로직
     *
     * Python(feedparser) 기준 대응:
     * - entry.content[0].value
     */
    private fun extractContent(item: Element): String {

        // content:encoded (가장 정확)
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
