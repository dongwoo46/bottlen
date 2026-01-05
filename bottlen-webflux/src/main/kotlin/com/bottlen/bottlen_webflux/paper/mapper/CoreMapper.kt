package com.bottlen.bottlen_webflux.paper.mapper

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
