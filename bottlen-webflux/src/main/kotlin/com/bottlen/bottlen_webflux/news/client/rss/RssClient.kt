package com.bottlen.bottlen_webflux.news.client.rss

import reactor.core.publisher.Flux
import com.bottlen.bottlen_webflux.news.dto.rss.RssArticle

interface RssClient {
    /**
     * RSS URL에서 XML을 비동기로 가져와서 파싱 후 기사 리스트 반환
     */
    fun fetchArticles(url: String, topic: String): Flux<RssArticle>

    /**
     * RSS 원본 XML을 특정 뉴스사 규칙에 맞게 파싱
     * (뉴스사별로 XML 구조가 달라서 구현체에서 처리)
     */
    fun parse(xml: String, topic: String): Flux<RssArticle>

}
