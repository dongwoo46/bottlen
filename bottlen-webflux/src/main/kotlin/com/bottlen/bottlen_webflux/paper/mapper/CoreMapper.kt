package com.bottlen.bottlen_webflux.paper.mapper

import com.bottlen.bottlen_webflux.paper.domain.Paper
import com.bottlen.bottlen_webflux.paper.dto.core.CoreWork

fun CoreWork.mergeInto(paper: Paper): Paper {
    return paper.copy(
        abstract = paper.abstract ?: abstractText,
        pdfUrl = paper.pdfUrl ?: downloadUrl,
        authors = if (paper.authors.isEmpty()) authors ?: emptyList() else paper.authors
    )
}
