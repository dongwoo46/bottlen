package com.bottlen.bottlen_webflux.disclosure.client

import com.bottlen.bottlen_webflux.disclosure.dto.DisclosureArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.security.MessageDigest
import java.time.Instant

/**
 * SEC EDGAR 공시 수집 클라이언트
 * - 모든 I/O는 Kotlin Coroutine 기반으로 실행됨
 * - RSS(XML) 기반 데이터 수집
 * - Reactor 타입(Flux/Mono)은 사용하지 않음
 *
 * 역할:
 * 1) RSS 요청(fetch)
 * 2) XML 파싱(parse)
 * 3) DisclosureArticle 변환
 */
@Component
class EdgarClient(
        webClientBuilder: WebClient.Builder,
) {

    /**
     * HTTP 요청을 위한 WebClient 인스턴스
     * - Reactor 기반이지만 awaitBody()를 사용하여 완전 코루틴 방식으로 동작
     * - 즉, 내부적으로는 논블로킹이며, 외부적으로는 suspend 사용
     */
    private val webClient = webClientBuilder.build()

    /**
     * SEC에서 수집할 대상 Form Type 목록
     * - 시장에 직접적인 가격 영향을 주는 이벤트 중심
     * - 8-K: 기업 이벤트 보고
     * - 13F-HR: 펀드 보유 자산 보고
     * - SC 13D/13G: 대규모 지분 취득 보고
     */
    private val allowedForms = setOf(
            "8-K", "4", "13F-HR", "SC 13D", "SC 13G", "6-K"
    )

    /**
     * -----------------------------------------------------------
     * ① EDGAR RSS를 요청하는 함수 (Coroutine)
     * -----------------------------------------------------------
     * - suspend로 실행 → I/O 중 스레드 차지 X
     * - EDGAR RSS는 XML이므로 문자열로 먼저 받아야 함
     * - Reactor 사용 X (오직 awaitBody만 사용)
     *
     * @param url SEC EDGAR RSS 주소
     * @return Flow<DisclosureArticle> 스트림
     */
    suspend fun fetch(url: String): Flow<DisclosureArticle> {
        val xml = webClient.get()
                .uri(url)
                .retrieve()
                .awaitBody<String>()    // Mono → suspend 변환

        return parse(xml)
    }

    /**
     * -----------------------------------------------------------
     * ② RSS XML → DisclosureArticle 변환 함수
     * -----------------------------------------------------------
     * - Flow 로 한 건씩 emit
     * - Reactor Flux가 아닌 순수 코루틴 스트림
     * - Jsoup XML 파서로 entry 파싱
     *
     * @param xml RSS 원문 XML
     * @return Flow<DisclosureArticle>
     */
    fun parse(xml: String): Flow<DisclosureArticle> = flow {

        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        val entries = doc.select("entry")   // SEC ATOM RSS 구조

        for (entry in entries) {

            // Form 종류 추출
            val formType = entry.selectFirst("category")
                    ?.attr("term")
                    ?.trim()
                    .orEmpty()

            // 수집 대상 Form이 아니면 SKIP
            if (formType !in allowedForms) continue

            // 제목
            val title = entry.selectFirst("title")
                    ?.text()
                    ?.trim()
                    .orEmpty()

            // 공시 원문 링크 (filing URL)
            val link = entry.selectFirst("link")
                    ?.attr("href")
                    ?.trim()
                    .orEmpty()

            if (link.isBlank()) continue

            // 기업 CIK 추출
            val cik = extractCik(entry)

            // 업데이트 날짜 추출
            val published = normalizeDate(
                    entry.selectFirst("updated")?.text().orEmpty()
            )

            // 해시 기반 고유 ID 생성
            val id = generateId(link)

            emit(
                    DisclosureArticle(
                            id = id,
                            source = "sec_edgar",
                            corpId = cik,
                            formType = formType,
                            title = title,
                            link = link,
                            published = published,
                            collectedAt = Instant.now().toString(),
                            // 내용 요약 또는 핵심포인트는 Service 단계에서 채움
                            highlights = null
                    )
            )
        }
    }

    /**
     * -----------------------------------------------------------
     * ③ entry 내부에서 CIK 추출
     * -----------------------------------------------------------
     * - EDGAR RSS <id> 항목에는 "CIK########" 형태 문자열 포함
     * - Regex 로 숫자만 추출
     */
    private fun extractCik(entry: org.jsoup.nodes.Element): String? {
        val idText = entry.selectFirst("id")?.text() ?: return null
        val pattern = Regex("CIK(\\d+)")
        return pattern.find(idText)?.groupValues?.getOrNull(1)
    }

    /**
     * -----------------------------------------------------------
     * ④ 날짜 문자열을 ISO-8601 형식으로 변환
     * -----------------------------------------------------------
     * - Instant.parse(...) 사용
     * - 실패 시 현재 시간으로 fallback
     */
    private fun normalizeDate(raw: String): String {
        return try {
            Instant.parse(raw).toString()
        } catch (_: Exception) {
            Instant.now().toString()
        }
    }

    /**
     * -----------------------------------------------------------
     * ⑤ 링크 기반 고유 ID 생성
     * -----------------------------------------------------------
     * - SHA-256(link) → hex string
     * - 링크가 EDGAR Filing 문서의 영구 식별 역할
     * - DB에서 중복 여부 판단하는 PK 역할
     */
    private fun generateId(link: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
                .digest(link.toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }
}
