package com.bottlen.bottlen_webflux.news.dto

import com.bottlen.news.dto.NewsDto
import com.bottlen.news.dto.NewsResponseDto
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * NYT Top Stories API Response DTO
 * https://api.nytimes.com/svc/topstories/v2/{section}.json
 */
data class NewYorkTimesResponseDto(
        val status: String,
        val copyright: String?,
        val section: String?,
        @JsonProperty("last_updated")
        val lastUpdated: String?,
        @JsonProperty("num_results")
        val numResults: Int?,
        val results: List<ArticleDto>
) : NewsResponseDto {

    /**
     * 공통 NewsDto로 변환
     */
    override fun toNewsDtoList(category: String): List<NewsDto> {
        val formatter = DateTimeFormatter.ISO_DATE_TIME

        return results.map {
            NewsDto(
                    title = it.title ?: "(제목 없음)",
                    summary = it.abstract ?: "",
                    url = it.url ?: "",
                    source = "NewYorkTimes",
                    category = category,
                    publishedAt = it.publishedDate?.let { date ->
                        try {
                            LocalDateTime.parse(date, formatter)
                        } catch (e: Exception) {
                            null
                        }
                    }
            )
        }
    }

    // ───────────────────────────────
    // 내부 DTO 매핑
    // ───────────────────────────────
    data class ArticleDto(
            val section: String?,
            val subsection: String?,
            val title: String?,
            val abstract: String?,
            val url: String?,
            val uri: String?,
            val byline: String?,
            @JsonProperty("item_type")
            val itemType: String?,
            @JsonProperty("updated_date")
            val updatedDate: String?,
            @JsonProperty("created_date")
            val createdDate: String?,
            @JsonProperty("published_date")
            val publishedDate: String?,
            @JsonProperty("material_type_facet")
            val materialTypeFacet: String?,
            val kicker: String?,
            @JsonProperty("des_facet")
            val desFacet: List<String>?,
            @JsonProperty("org_facet")
            val orgFacet: List<String>?,
            @JsonProperty("per_facet")
            val perFacet: List<String>?,
            @JsonProperty("geo_facet")
            val geoFacet: List<String>?,
            val multimedia: List<MultimediaDto>?,
            @JsonProperty("short_url")
            val shortUrl: String?
    )

    data class MultimediaDto(
            val url: String?,
            val format: String?,
            val height: Int?,
            val width: Int?,
            val type: String?,
            val subtype: String?,
            val caption: String?,
            val copyright: String?
    )
}
