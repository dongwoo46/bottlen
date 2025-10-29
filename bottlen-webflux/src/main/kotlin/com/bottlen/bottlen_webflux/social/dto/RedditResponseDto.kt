package com.bottlen.bottlen_webflux.social.dto

import com.bottlen.bottlen_webflux.social.domain.PlatformCategory
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * Reddit API 응답을 SocialDto 리스트로 변환하는 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RedditResponseDto(
        val data: RedditListingData? = null
) : SocialResponseDto {

    override fun toSocialDtoList(): List<SocialDto> {
        return data?.children.orEmpty().mapNotNull { child ->
            val post = child.data ?: return@mapNotNull null
            if (post.selftext.isNullOrBlank()) return@mapNotNull null

            SocialDto(
                    platform = PlatformCategory.REDDIT,
                    source = "r/${post.subreddit}",
                    sourceId = post.id ?: "",
                    author = post.author,
                    title = post.title,
                    content = post.selftext.trim(),
                    url = "https://reddit.com${post.permalink}",
                    createdAt = Instant.ofEpochSecond(post.createdUtc?.toLong() ?: 0L)
            )
        }
    }
}

/** Reddit API JSON 구조 매핑용 내부 클래스들 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RedditListingData(
        val children: List<RedditChild>? = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RedditChild(
        val data: RedditPost? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RedditPost(
        val id: String? = null,
        val subreddit: String? = null,
        val author: String? = null,
        val title: String? = null,
        val selftext: String? = null,
        val permalink: String? = null,
        @JsonProperty("created_utc")
        val createdUtc: Double? = null // ✅ Double로 변경
)
