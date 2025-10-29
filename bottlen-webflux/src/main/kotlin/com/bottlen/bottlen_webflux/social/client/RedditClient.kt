package com.bottlen.bottlen_webflux.social.client

import com.bottlen.bottlen_webflux.social.dto.RedditResponseDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.Base64  // ✅ 수정된 import

@Component
class RedditClient(
        webClientBuilder: WebClient.Builder,
        @Value("\${external.social.reddit.auth-base-url}") private val authBaseUrl: String,
        @Value("\${external.social.reddit.api-base-url}") private val apiBaseUrl: String,
        @Value("\${external.social.reddit.client-id}") private val clientId: String,
        @Value("\${external.social.reddit.client-secret}") private val clientSecret: String,
        private val objectMapper: ObjectMapper
) {

    private val authClient = webClientBuilder
            .baseUrl(authBaseUrl)
            .defaultHeader("User-Agent", "BottlenBot/1.0 (by u/sniper)")
            .build()

    private val apiClient = webClientBuilder
            .baseUrl(apiBaseUrl)
            .defaultHeader("User-Agent", "BottlenBot/1.0 (by u/sniper)")
            .build()

    private fun getAccessToken(): Mono<String> {
        val encoded = Base64.getEncoder()
                .encodeToString("$clientId:$clientSecret".toByteArray())

        return authClient.post()
                .uri("/api/v1/access_token")
                .headers {
                    it.set("Authorization", "Basic $encoded")
                    it.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                }
                .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
                .map { it["access_token"] as String }
    }

    /** ✅ 단일 fetchSubreddit() — Raw 출력 + DTO 변환까지 한번에 */
    fun fetchSubreddit(subreddit: String, limit: Int = 10): Mono<RedditResponseDto> {
        return getAccessToken().flatMap { token ->
            apiClient.get()
                    .uri("/r/$subreddit/new.json?limit=$limit")
                    .headers { it.set("Authorization", "Bearer $token") }
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .doOnNext { json ->
                        println("\n---------- [Reddit: $subreddit] 원본 JSON ----------")
                        println(json.take(500) + if (json.length > 500) "..." else "")
                        println("--------------------------------------------------\n")
                    }
                    .map { json ->
                        try {
                            val dto = objectMapper.readValue(json, RedditResponseDto::class.java)
                            println("✅ [Reddit: $subreddit] JSON → DTO 변환 성공 (${dto.data?.children?.size ?: 0}건)")
                            println("--------------------------------------------------\n")
                            dto
                        } catch (e: Exception) {
                            println("❌ [Reddit: $subreddit] JSON 변환 실패: ${e.message}")
                            println("--------------------------------------------------\n")
                            throw e
                        }
                    }
        }
    }
}
