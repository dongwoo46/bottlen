package com.bottlen.bottlen_webflux.paper.mapper

import com.bottlen.bottlen_webflux.paper.domain.Paper
import com.bottlen.bottlen_webflux.paper.domain.PaperDocument
import com.bottlen.bottlen_webflux.paper.domain.PaperSource
import com.bottlen.bottlen_webflux.paper.dto.core.CoreWork

fun CoreWork.toPaperDocument(): PaperDocument =
    PaperDocument(
        id = id ?: error("CORE id is required"),
        title = title,
        abstractText = abstractText,
        fullText = fullText,
        doi = doi,
        downloadUrl = downloadUrl,
        source = PaperSource.CORE
    )


fun PaperDocument.mergeInto(existing: Paper?): Paper {
    return if (existing == null) {
        // 최초 생성
        Paper(
            id = id,
            title = title ?: "",
            abstract = abstractText,
            pdfUrl = downloadUrl,
            authors = emptyList(), // 비즈니스 기준에서 채움
            doi = doi
        )
    } else {
        // 보완/병합
        existing.copy(
            abstract = existing.abstract ?: abstractText,
            pdfUrl = existing.pdfUrl ?: downloadUrl
        )
    }
}

