package com.bottlen.bottlen_webflux.infra.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig {
    
    // 임시 용
    @Bean("basicWebClient")
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder()
    }

    @Bean("rssWebClient")
    fun rssWebClient(): WebClient {

        // 1. 커넥션 풀 설정
        val connectionProvider = ConnectionProvider.builder("rss-connection-pool")
            .maxConnections(500)                              // 최대 500개 동시 커넥션
            .maxIdleTime(Duration.ofSeconds(20))              // 20초간 유휴 시 제거
            .maxLifeTime(Duration.ofMinutes(5))               // 5분 후 커넥션 재생성
            .pendingAcquireTimeout(Duration.ofSeconds(45))    // 커넥션 대기 최대 45초
            .evictInBackground(Duration.ofSeconds(120))       // 2분마다 유휴 커넥션 정리
            .build()

        // 2. HTTP 클라이언트 설정
        val httpClient = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)  // TCP 연결 타임아웃: 5초
            .doOnConnected { conn ->
                // 읽기 타임아웃: 10초 (서버가 응답 안 보내면 끊음)
                conn.addHandlerLast(ReadTimeoutHandler(10, TimeUnit.SECONDS))
                // 쓰기 타임아웃: 10초 (서버가 요청 안 받으면 끊음)
                conn.addHandlerLast(WriteTimeoutHandler(10, TimeUnit.SECONDS))
            }
            .responseTimeout(Duration.ofSeconds(30))  // 전체 응답 타임아웃: 30초

        // 3. 메모리 버퍼 제한
        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { configurer ->
                configurer.defaultCodecs()
                    .maxInMemorySize(10 * 1024 * 1024)  // 10MB로 제한
            }
            .build()

        // 4. WebClient 생성
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(exchangeStrategies)
            .defaultHeader(HttpHeaders.USER_AGENT, "BottleN-RSS-Collector/1.0")
            .build()
    }
}
