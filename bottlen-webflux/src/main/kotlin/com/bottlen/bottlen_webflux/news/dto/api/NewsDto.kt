package com.bottlen.bottlen_webflux.news.dto.api

import java.time.LocalDateTime

/**
 * 내부에서 사용하는 통합 뉴스 데이터 모델
 * → 모든 외부 API는 이 구조로 변환되어 들어온다.
 */
data class NewsDto(
        val title: String,
        val summary: String?,
        val url: String,
        val source: String,
        val category: String,
        val publishedAt: LocalDateTime?
)
