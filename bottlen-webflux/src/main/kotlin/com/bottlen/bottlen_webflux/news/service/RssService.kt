package com.bottlen.bottlen_webflux.news.service

import com.bottlen.bottlen_webflux.news.domain.RssFeed
import com.bottlen.bottlen_webflux.news.domain.Topic
import com.bottlen.bottlen_webflux.news.dto.rss.RssFeedConfig
import com.bottlen.bottlen_webflux.news.repository.RssFeedRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * RSS 도메인의 중심 서비스
 *
 * - RSS Feed 관리(CRUD)
 * - 실행 정책 판단
 * - Ingest 경계에서 Config 생성
 */
@Service
class RssService(
    private val rssFeedRepository: RssFeedRepository,
    private val ingestService: RssIngestService
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    /* =========================
     * Admin / CRUD
     * ========================= */

    suspend fun createFeed(feed: RssFeed): RssFeed =
        rssFeedRepository.save(feed)

    suspend fun updateFeed(feedId: Long, updater: (RssFeed) -> RssFeed): RssFeed {
        val feed = getFeed(feedId)
        return rssFeedRepository.save(
            updater(feed).copy(updatedAt = Instant.now())
        )
    }

    suspend fun deleteFeed(feedId: Long) {
        rssFeedRepository.deleteById(feedId)
    }

    suspend fun getFeed(feedId: Long): RssFeed =
        rssFeedRepository.findById(feedId)
            ?: throw IllegalArgumentException("RssFeed not found: $feedId")

    suspend fun getAllEnabledFeeds(): List<RssFeed> =
        rssFeedRepository.findAllByEnabledTrue().toList()

    /* =========================
     * Execution
     * ========================= */

    /**
     * Scheduler 전용 진입점
     * 스케쥴러에서 feed를 모으기 위한 데이터 요청
     * db에서 저장된 정보를 보고 스케쥴링할 데이터만 요청
     */
    suspend fun executeRunnableFeeds() {
        val now = Instant.now()

        val feeds: List<RssFeed> =
            rssFeedRepository.findAllByEnabledTrue()
                .filter { it.shouldRun(now) }
                .toList()

        log.info(
            "[SCHEDULER][RSS FEED] runnable feeds filtered. count={}",
            feeds.size
        )

        feeds.forEach { feed ->
            execute(feed)
        }
    }

    /**
     * 관리자 페이지에서 특정 Feed 즉시 실행
     */
    suspend fun executeFeed(feedId: Long) {
        val feed = getFeed(feedId)
        execute(feed)
    }

    /* =========================
     * Internal
     * ========================= */

    /**
     * RssFeed → RssFeedConfig 변환 후 ingest 수행
     * (도메인 → 수집 파이프라인 경계)
     */
    private suspend fun execute(feed: RssFeed) {
        val config = feed.toConfig()

        ingestService.ingest(config)

        rssFeedRepository.save(
            feed.copy(
                lastIngestedAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )
    }

    /**
     * 도메인 객체를 수집용 Config로 변환
     *
     * - 변환 책임은 RssService에 있다
     * - IngestService는 Domain(RssFeed, Topic)을 알지 못한다
     */
    private fun RssFeed.toConfig(): RssFeedConfig {
        return RssFeedConfig(
            source = source,
            topic = topic,                  // 이미 Topic이므로 그대로 사용
            rssUrl = url,
            sourceTopic = topic.code        // 외부 RSS용 원본/표현 값
        )
    }
}
