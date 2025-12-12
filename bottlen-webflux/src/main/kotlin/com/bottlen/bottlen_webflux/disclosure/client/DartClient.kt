package com.bottlen.bottlen_webflux.disclosure.client

import com.bottlen.bottlen_webflux.disclosure.dto.DartDocumentResponse
import com.bottlen.bottlen_webflux.disclosure.dto.DartListResponse
import com.bottlen.bottlen_webflux.disclosure.dto.DisclosureArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.security.MessageDigest
import java.time.Instant

@Component
class DartClient(
        webClientBuilder: WebClient.Builder,

        @Value("\${external.disclosure.dart.api-key}")
        private val apiKey: String,

        @Value("\${external.disclosure.dart.list-url}")
        private val listUrl: String,

        @Value("\${external.disclosure.dart.document-url}")
        private val documentUrl: String
) {

    private val webClient = webClientBuilder.build()

    /**
     * DART 공시 목록 중 "시장 영향도가 큰 공시 유형"만 필터링하기 위한 리스트.
     * report_nm(공시 제목) 내 포함 여부로 판별한다.
     */
    private val allowedForms = setOf(
            "주요사항보고서",
            "증권발행실적보고서",
            "단일판매ㆍ공급계약체결",
            "자기주식취득",
            "자기주식처분",
            "합병",
            "영업양수",
            "영업양도",
            "전환사채발행",
            "신주인수권부사채발행",
            "유상증자결정",
            "감자결정"
    )

    /**
     * (1) 날짜 범위로 DART 목록(list.json)을 조회한다.
     * (2) allowedForms 기준으로 시장 영향도가 큰 공시만 걸러낸다.
     * (3) DisclosureArticle(메타데이터)로 변환하여 Flow로 반환한다.
     *
     * ※ 역할: "공시 리스트 수집" 전용.
     * ※ 본문(content) 수집은 fetchDocument() + extractDocumentContent()에서 처리한다.
     */
    suspend fun fetchList(startDate: String, endDate: String): Flow<DisclosureArticle> {
        val url =
                "$listUrl.json?crtfc_key=$apiKey&bgn_de=$startDate&end_de=$endDate&page_no=1&page_count=100"

        val response = webClient.get()
                .uri(url)
                .retrieve()
                .awaitBody<DartListResponse>()

        val list = response.list ?: emptyList()

        return flow {
            for (item in list) {
                val formName = item.report_nm?.trim().orEmpty()

                // 허용되지 않은 공시 유형은 스킵
                if (allowedForms.none { formName.contains(it) }) continue

                val link = "https://dart.fss.or.kr/dsaf001/main.do?rcpNo=${item.rcept_no}"

                emit(
                        DisclosureArticle(
                                id = generateId(link),
                                source = "dart",
                                corpId = item.corp_code.orEmpty(),
                                formType = formName,
                                title = item.report_nm.orEmpty(),
                                link = link,
                                published = normalizeDate(item.rcept_dt.orEmpty()),
                                collectedAt = Instant.now().toString(),
                                highlights = null  // 실제 요약은 Service 단계에서 설정
                        )
                )
            }
        }
    }

    /**
     * 특정 공시(rcept_no)의 "원문(document.json)"을 불러온다.
     * document.json은 본문/첨부/별첨 등의 HTML/XML이 포함된 섹션 리스트를 제공한다.
     *
     * ※ 역할: 공시 원문 데이터 전체를 받아오는 기능.
     */
    suspend fun fetchDocument(rcpNo: String): DartDocumentResponse {
        val url = "$documentUrl.json?crtfc_key=$apiKey&rcept_no=$rcpNo"

        return webClient.get()
                .uri(url)
                .retrieve()
                .awaitBody<DartDocumentResponse>()
    }

    /**
     * document.json 응답 안에서
     * - "본문"
     * - "첨부문서"
     * 섹션 중 하나를 골라 HTML을 텍스트로 변환해 반환한다.
     *
     * ※ 역할: DART 문서에서 "실제 텍스트 컨텐츠"만 추출하는 기능.
     */
    fun extractDocumentContent(doc: DartDocumentResponse): String {
        val body = doc.list
                ?.firstOrNull { it.se == "본문" || it.se == "첨부문서" }
                ?.content
                .orEmpty()

        return org.jsoup.Jsoup.parse(body).text().trim()
    }

    /**
     * DART 날짜(yyyyMMdd)를 ISO8601 날짜 문자열로 변환한다.
     * 예: "20240102" → "2024-01-02T00:00:00Z"
     */
    private fun normalizeDate(raw: String): String {
        return try {
            val y = raw.substring(0, 4)
            val m = raw.substring(4, 6)
            val d = raw.substring(6, 8)
            Instant.parse("${y}-${m}-${d}T00:00:00Z").toString()
        } catch (_: Exception) {
            Instant.now().toString()
        }
    }

    /**
     * 공시 링크를 기반으로 SHA-256 해시 값을 생성한다.
     * DisclosureArticle의 id(중복 방지 PK처럼 사용)에 활용된다.
     */
    private fun generateId(link: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
                .digest(link.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
