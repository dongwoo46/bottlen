package com.bottlen.news.dto

import java.time.*
import java.time.format.DateTimeFormatter

data class GdeltResponseDto(
        val articles: List<GdeltArticle>
) : NewsResponseDto {
    override fun toNewsDtoList(category: String): List<NewsDto> {
        return articles.map {
            NewsDto(
                    title = it.title ?: "(제목 없음)",
                    summary = null, // GDELT는 summary 필드 없음
                    url = it.url ?: "",
                    source = it.domain ?: "unknown",
                    category = category,
                    publishedAt = it.seendate?.let { dateStr ->
                        try {
                            LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
                        } catch (_: Exception) {
                            null
                        }
                    }
            )
        }
    }
}

data class GdeltArticle(
        val title: String?,
        val url: String?,
        val domain: String?,
        val tone: Double?,
        val language: String?,
        val seendate: String?
)
