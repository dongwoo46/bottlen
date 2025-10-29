package com.bottlen.bottlen_webflux.social.dto

import com.bottlen.bottlen_webflux.social.domain.PlatformCategory
import java.time.Instant

data class SocialDto(
        val platform: PlatformCategory,           // reddit, telegram, twitter 등
        val source: String?,            // subreddit명, channel명, chat title 등
        val sourceId: String,           // 게시물 or 메시지 고유 ID
        val author: String?,            // 작성자 이름 / 닉네임
        val title: String? = null,      // reddit만 사용
        val content: String,            // 본문 텍스트
        val url: String? = null,        // reddit 원문 링크 등
        val createdAt: Instant          // 원본 생성 시각
)

