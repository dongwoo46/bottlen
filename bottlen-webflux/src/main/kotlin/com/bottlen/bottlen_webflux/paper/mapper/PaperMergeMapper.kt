package com.bottlen.bottlen_webflux.paper.mapper

import com.bottlen.bottlen_webflux.paper.domain.Paper
import com.bottlen.bottlen_webflux.paper.domain.PaperDocument

/**
 * PaperDocument → Canonical Paper 변환 및 병합
 *
 * 정책:
 * - DOI 없는 논문은 배제
 * - Paper.id는 DOI 기반 canonical ID
 */
fun PaperDocument.mergeInto(existing: Paper?): Paper? {
    val canonicalDoi = doi ?: return null

    return if (existing == null) {
        Paper(
            id = canonicalDoi,
            doi = canonicalDoi,
            title = title ?: "",
            abstract = abstractText,
            pdfUrl = downloadUrl,
            authors = emptyList()
        )
    } else {
        existing.copy(
            abstract = existing.abstract ?: abstractText,
            pdfUrl = existing.pdfUrl ?: downloadUrl
        )
    }
}
