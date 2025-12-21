package com.bottlen.bottlen_webflux.news.client.rss

import com.bottlen.bottlen_webflux.news.dto.rss.RssArticle
import com.bottlen.bottlen_webflux.news.dto.rss.RssFeedConfig
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

/**
 * RSS 수집용 베이스 클라이언트
 *
 * - fetch / retry / error handling 공통 처리
 * - source별 정책은 override 가능
 */
abstract class AbstractRssClient(
    webClientBuilder: WebClient.Builder
) : RssClient {

    protected val webClient: WebClient = webClientBuilder.build()
    protected val logger = LoggerFactory.getLogger(javaClass)

    // 구현체가 다시 fetchArticles를 직접 구현하는것 방지 => final
    final override fun fetchArticles(feed: RssFeedConfig): Flux<RssArticle> {
        return fetchXml(feed)
            .flatMapMany { xml -> parseInternal(xml, feed) }
            .onErrorResume { error ->
                handleParsingError(feed, error)

                // 파싱 에러 발생 시 운영환경에서는 무시하고 계속해서 수집할 수 있도록하고 개발환경에서는 에러출력해야함
                if (swallowParsingError()) {
                    Flux.empty()
                } else {
                    Flux.error(error)
                }
            }
    }

    /**
     * RSS XML 다운로드
     */
    private fun fetchXml(feed: RssFeedConfig): Mono<String> {
        return webClient.get()
            .uri(feed.rssUrl)
            .retrieve()
            .bodyToMono(String::class.java)
            .timeout(fetchTimeout())
            .let { mono ->
                retryPolicy()?.let { mono.retryWhen(it) } ?: mono
            }
            .onErrorResume { error ->
                handleFetchError(feed, error)
                Mono.empty()
            }
    }

    /**
     * 재시도 정책 (source별 override 가능)
     *
     * 기본값:
     * - 5xx 에러만
     * - 최대 3회
     * - exponential backoff
     */
    protected open fun retryPolicy(): Retry? {
        return Retry.backoff(3, Duration.ofSeconds(1))
            .filter { error ->
                error is WebClientResponseException &&
                        error.statusCode.value() in 500..599
            }
            .doBeforeRetry { signal ->
                logger.warn(
                    "Retrying RSS fetch: source={}, url={}, attempt={}",
                    signal.totalRetries() + 1
                )
            }
    }

    /**
     * fetch 타임아웃
     */
    protected open fun fetchTimeout(): Duration =
        Duration.ofSeconds(30)

    /**
     * 파싱 에러를 삼킬지 여부
     *
     * - 운영 환경: true (fail-soft)
     * - 개발/테스트: false (fail-fast)
     */
    protected open fun swallowParsingError(): Boolean = true

    /**
     * HTTP / 네트워크 에러 처리
     */
    protected open fun handleFetchError(
        feed: RssFeedConfig,
        error: Throwable
    ) {
        when (error) {
            is WebClientResponseException -> {
                val status = error.statusCode.value()
                when {
                    status in 400..499 -> {
                        logger.error(
                            "RSS client error: source={}, url={}, status={}, body={}",
                            feed.source,
                            feed.rssUrl,
                            status,
                            error.responseBodyAsString.take(200)
                        )
                    }
                    status in 500..599 -> {
                        logger.error(
                            "RSS server error: source={}, url={}, status={}",
                            feed.source,
                            feed.rssUrl,
                            status
                        )
                    }
                }
            }
            else -> {
                logger.error(
                    "RSS network error: source={}, url={}, message={}",
                    feed.source,
                    feed.rssUrl,
                    error.message,
                    error
                )
            }
        }
    }

    /**
     * XML 파싱 에러 처리
     */
    protected open fun handleParsingError(
        feed: RssFeedConfig,
        error: Throwable
    ) {
        logger.error(
            "RSS parsing error: source={}, url={}, message={}",
            feed.source,
            feed.rssUrl,
            error.message,
            error
        )
    }

    /**
     * 뉴스사별 RSS 파싱 구현
     */
    protected abstract fun parseInternal(
        xml: String,
        feed: RssFeedConfig
    ): Flux<RssArticle>
}
