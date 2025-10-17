package com.bottlen.bottlen_webflux.news.dto

import com.bottlen.news.dto.NewsDto
import com.bottlen.news.dto.NewsResponseDto
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * ✅ NewsData.io Latest News API 응답 DTO
 * - https://newsdata.io/api/1/latest
 * - nextPage 포함 (페이징 지원)
 * - country, category, keywords, creator 등 다중 필드 대응
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NewsDataResponseDto(
        val status: String?,
        @JsonProperty("totalResults")
        val totalResults: Int?,
        val results: List<NewsItem>?,
        @JsonProperty("nextPage")
        val nextPage: String? = null // ✅ 다음 페이지 토큰 추가
) : NewsResponseDto {

    override fun toNewsDtoList(category: String): List<NewsDto> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        return results?.map { item ->
            NewsDto(
                    title = item.title ?: "(제목 없음)",
                    summary = item.description ?: "",
                    url = item.link ?: "",
                    source = item.source_id ?: "(unknown)",
                    // ✅ category가 비어 있으면 응답 category 필드 사용
                    category = category.ifBlank { item.category?.joinToString(",") ?: "(unknown)" },
                    publishedAt = item.pubDate?.let { date ->
                        runCatching { LocalDateTime.parse(date, formatter) }.getOrNull()
                    }
            )
        } ?: emptyList()
    }

    /**
     * ✅ NewsData.io의 results 내부 구조
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class NewsItem(
            val title: String?,
            val link: String?,
            val description: String?,
            val pubDate: String?,
            val source_id: String?,
            val country: List<String>?,      // ✅ 배열 형태
            val category: List<String>?,     // ✅ 배열 형태
            val language: String?,
            val image_url: String? = null,   // 선택적 이미지 URL
            val content: String? = null,     // 전문 내용 (있을 수도 있음)
            val keywords: List<String>? = null,
            val creator: List<String>? = null
    )
}
