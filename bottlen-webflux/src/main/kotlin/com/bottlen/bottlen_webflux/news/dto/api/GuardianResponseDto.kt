package com.bottlen.bottlen_webflux.news.dto.api

import java.time.LocalDateTime

data class GuardianResponseDto(
        val response: GuardianResponseBody
) : NewsResponseDto {

    override fun toNewsDtoList(category: String): List<NewsDto> {
        return response.results.map {
            NewsDto(
                    title = it.webTitle,
                    summary = it.fields?.trailText,
                    url = it.webUrl,
                    source = "Guardian",
                    category = category,
                    publishedAt = it.webPublicationDate?.let { date ->
                        LocalDateTime.parse(date.removeSuffix("Z"))
                    }
            )
        }
    }
}

data class GuardianResponseBody(
        val results: List<GuardianArticle>
)

data class GuardianArticle(
        val webTitle: String,
        val webUrl: String,
        val webPublicationDate: String?,
        val sectionName: String?,
        val fields: GuardianFields?
)

data class GuardianFields(
        val trailText: String?
)