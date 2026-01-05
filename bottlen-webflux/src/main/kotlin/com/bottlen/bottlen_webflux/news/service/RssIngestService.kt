package com.bottlen.bottlen_webflux.news.service

import com.bottlen.bottlen_webflux.infra.redis.BloomFilterClient
import com.bottlen.bottlen_webflux.news.dto.rss.RssArticle
import com.bottlen.bottlen_webflux.news.dto.rss.RssFeedConfig
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class RssIngestService(
    private val fetchService: RssFetchService,
    private val bloom: BloomFilterClient
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    // rss feed 요청 하고 bloomfilter를 이용하여 article 필터링
    suspend fun ingest(feed: RssFeedConfig) {
        val filterName = "BF:news:${feed.source}"
        val bloomAvailable = bloom.init(filterName)
            .onFailure {
                log.warn(
                    "[RSS][BLOOM] init failed, continue without dedup. source={}",
                    feed.source,
                    it
                )
            }
            .isSuccess

        var totalCount = 0
        var newCount = 0

        fetchService.fetch(feed)
            .asFlow()
            .collect { article ->

                totalCount++

                val articleKey = generateArticleKey(article)

                // Bloom 사용 가능할 때만 dedup 수행
                val isNew = if (bloomAvailable) {
                    try {
                        bloom.add(filterName, articleKey)
                    } catch (e: Exception) {
                        log.warn(
                            "[RSS][BLOOM] add failed, fallback to accept. source={}",
                            feed.source,
                            e
                        )
                        true
                    }
                } else {
                    true
                }

                if (isNew) newCount++

                log.info(
                    "[RSS][ARTICLE] total={}, isNew={}, title={}, link={}",
                    totalCount,
                    isNew,
                    article.title,
                    article.link
                )

                if (!isNew) return@collect

                // TODO
                // DB save
                // Kafka publish
            }

        log.info(
            "[RSS][SUMMARY] source={}, total={}, new={}",
            feed.source,
            totalCount,
            newCount
        )
    }

    private fun generateArticleKey(article: RssArticle): String {
        val raw = "${article.source}|${normalizeLink(article.link)}"
        return sha256(raw)
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun normalizeLink(link: String): String {
        return link
            .substringBefore("?")        // utm 제거
            .trim()
            .lowercase()
    }
}
