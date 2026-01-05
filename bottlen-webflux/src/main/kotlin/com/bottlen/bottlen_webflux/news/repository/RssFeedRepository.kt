package com.bottlen.bottlen_webflux.news.repository

import com.bottlen.bottlen_webflux.news.domain.RssFeed
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RssFeedRepository : CoroutineCrudRepository<RssFeed, Long> {

    /**
     * 활성화된 모든 RSS Feed 조회
     *
     * - Scheduler에서 주기적으로 호출
     */
    fun findAllByEnabledTrue(): Flow<RssFeed>

    /**
     * 특정 소스의 모든 Feed 조회
     *
     * - 관리자 페이지용
     */
    fun findAllBySource(source: String): Flow<RssFeed>

    /**
     * 특정 소스 + 토픽 Feed 조회
     *
     * - 중복 생성 방지
     * - 단건 수정/조회 시 사용
     */
    suspend fun findBySourceAndTopic(
        source: String,
        topic: String
    ): RssFeed?
}
