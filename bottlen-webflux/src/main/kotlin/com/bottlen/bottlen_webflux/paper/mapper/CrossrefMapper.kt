package com.bottlen.bottlen_webflux.paper.mapper

import com.bottlen.bottlen_webflux.paper.domain.PaperDocument
import com.bottlen.bottlen_webflux.paper.domain.PaperSource
import com.bottlen.bottlen_webflux.paper.dto.crossref.CrossrefWork

fun CrossrefWork.toPaperDocument(): PaperDocument? {
    val doiValue = doi ?: return null
    val titleValue = title?.firstOrNull() ?: return null

    return PaperDocument(
        id = "crossref:$doiValue", // source-specific ID
        title = titleValue,
        abstractText = null,
        fullText = null,
        doi = doiValue,
        downloadUrl = null,
        source = PaperSource.CROSSREF
    )
}
