package com.bottlen.bottlen_webflux.news.dto.rss

import com.bottlen.bottlen_webflux.news.domain.Topic

/**
 * 하나의 RSS 피드 설정
 * (뉴스 소스 + 내부 토픽 + RSS URL 매핑 정보)
 */
data class RssFeedConfig(

    /** 뉴스 소스 식별자 (ex. reuters, cnbc) */
    val source: String,

    /** 내부에서 사용하는 표준 토픽 */
    val topic: Topic,

    /** 실제 RSS 요청 URL */
    val rssUrl: String,

    /** 뉴스사 RSS에서 사용하는 원본 토픽 이름 */
    val sourceTopic: String
)
