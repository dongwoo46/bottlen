package com.bottlen.bottlen_webflux.social.scheduler

import com.bottlen.bottlen_webflux.social.service.RedditService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers
import java.io.File

@Component
class SocialScheduler(
        private val redditService: RedditService
) {
    private val mapper = ObjectMapper(YAMLFactory())

    data class RedditConfig(
            val info: List<String> = emptyList(),
            val meme: List<String> = emptyList(),
            val research: List<String> = emptyList()
    )

    private fun loadRedditConfig(): RedditConfig {
        val path = "src/main/resources/config/reddit_subreddits.yml"
        val file = File(path)
        if (!file.exists()) error("reddit_subreddits.yml 파일을 찾을 수 없습니다. ($path)")
        return mapper.readValue(file, RedditConfig::class.java)
    }

//    /** ✅ INFO 그룹 (뉴스/리서치 중심) — 10분마다 */
//    @Scheduled(fixedDelay = 10 * 60 * 1000)
//    fun collectInfoSubreddits() {
//        val cfg = loadRedditConfig()
//        cfg.info.forEach { subreddit ->
//            redditService.fetch(subreddit, 20)
//                    .subscribeOn(Schedulers.boundedElastic())
//                    .subscribe(
//                            { dto -> println("🟢 [INFO] ${dto.source} - ${dto.title}") },
//                            { err -> println("❌ Reddit INFO fetch failed for $subreddit: ${err.message}") }
//                    )
//        }
//    }
//
//    /** ✅ MEME 그룹 (밈/심리 중심) — 5분마다 */
//    @Scheduled(fixedDelay = 5 * 60 * 1000)
//    fun collectMemeSubreddits() {
//        val cfg = loadRedditConfig()
//        cfg.meme.forEach { subreddit ->
//            redditService.fetch(subreddit, 20)
//                    .subscribeOn(Schedulers.boundedElastic())
//                    .subscribe(
//                            { dto -> println("🔥 [MEME] ${dto.source} - ${dto.title}") },
//                            { err -> println("❌ Reddit MEME fetch failed for $subreddit: ${err.message}") }
//                    )
//        }
//    }
//
//    /** ✅ RESEARCH 그룹 (느린 게시판) — 30분마다 */
//    @Scheduled(fixedDelay = 30 * 60 * 1000)
//    fun collectResearchSubreddits() {
//        val cfg = loadRedditConfig()
//        cfg.research.forEach { subreddit ->
//            redditService.fetch(subreddit, 15)
//                    .subscribeOn(Schedulers.boundedElastic())
//                    .subscribe(
//                            { dto -> println("📊 [RESEARCH] ${dto.source} - ${dto.title}") },
//                            { err -> println("❌ Reddit RESEARCH fetch failed for $subreddit: ${err.message}") }
//                    )
//        }
//    }
}
