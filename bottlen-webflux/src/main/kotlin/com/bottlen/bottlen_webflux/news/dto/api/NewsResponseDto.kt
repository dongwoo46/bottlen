package com.bottlen.bottlen_webflux.news.dto.api

/**
 * 외부 API 응답(JSON)을 NewsDto 리스트로 변환하기 위한 공통 규약
 */
interface NewsResponseDto {
    fun toNewsDtoList(category: String): List<NewsDto>
}
