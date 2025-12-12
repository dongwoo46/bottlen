package com.bottlen.bottlen_webflux.news.scheduler

import com.bottlen.bottlen_webflux.news.domain.NewsCategoryGroup
import com.bottlen.bottlen_webflux.news.domain.NewsSource
import com.bottlen.bottlen_webflux.news.service.NewsService
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NewsScheduler(
        private val newsService: NewsService
) {

    @Scheduled(fixedRate = 3600000)
    fun fetchAllNewsPeriodically() = runBlocking {
//        println("🕐 [Scheduler] 뉴스 수집 시작")

//        val guardianResult = newsService.fetchNews(
//                NewsCategoryGroup.GUARDIAN.map { it.label },
//                NewsSource.GUARDIAN
//        )
//        guardianResult.take(5).forEach { println(it) } // ✅ 5개만 출력
//        println("✅ [Guardian] ${guardianResult.size}건 수집 완료")

//        val nytResult = newsService.fetchNews(
//                NewsCategoryGroup.NYT.map { it.label },
//                NewsSource.NYT
//        )
//        nytResult.take(5).forEach { println(it) } // ✅ 5개만 출력
//        println("✅ [NYT] ${nytResult.size}건 수집 완료")
//
//        val catcherResult = newsService.fetchNewsWithSources(
//                NewsCategoryGroup.NEWS_CATCHER.map { it.label },
//                listOf("reuters.com", "cnn.com", "nytimes.com"),
//                NewsSource.NEWS_CATCHER
//        )
//        println("✅ [NewsCatcher] ${catcherResult.size}건 수집 완료")
//

//    val result = newsService.fetchByDomainAndCategoryNews(
//            domains = listOf("reuters.com", "businesswire.com"),
//            categories = listOf("technology", "science"),
//            source = NewsSource.NEWS_DATA
//    )
//
//    result.forEach { (domain, categoryMap) ->
//        println("🌐 $domain")
//        categoryMap.forEach { (category, articles) ->
//            println("   📂 $category → ${articles.size}개")
//            articles.take(3).forEach { println("      - ${it.title}") }
//        }
//    }
//        println("🏁 [Scheduler] 뉴스 수집 종료")
    }
}
