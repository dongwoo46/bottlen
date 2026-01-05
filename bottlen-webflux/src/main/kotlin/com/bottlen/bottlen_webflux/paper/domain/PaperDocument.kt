package com.bottlen.bottlen_webflux.paper.domain

/**
 * 외부 소스에서 수집한 논문 Raw 표현
 * - id: source-specific identifier
 * - doi: canonical identifier (있으면)
 */
data class PaperDocument(
    val id: String,
    val title: String?,
    val abstractText: String?,
    val fullText: String?,
    val doi: String?,
    val downloadUrl: String?,
    val source: PaperSource
)
