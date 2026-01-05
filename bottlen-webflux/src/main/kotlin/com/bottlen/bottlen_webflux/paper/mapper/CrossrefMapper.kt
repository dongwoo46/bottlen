package com.bottlen.bottlen_webflux.paper.mapper

import com.bottlen.bottlen_webflux.paper.domain.Paper
import com.bottlen.bottlen_webflux.paper.dto.crossref.CrossrefWork

fun CrossrefWork.toPaper(): Paper? {
    val doiValue = doi ?: return null
    val titleValue = title?.firstOrNull() ?: return null

    return Paper(
        id = doiValue,
        doi = doiValue,
        url = URL,

        title = titleValue,
        abstract = null,

        year = issued
            ?.dateParts
            ?.firstOrNull()
            ?.firstOrNull(),

        authors = author
            ?.mapNotNull {
                listOfNotNull(it.given, it.family)
                    .joinToString(" ")
                    .takeIf { name -> name.isNotBlank() }
            }
            ?: emptyList(),

        venue = containerTitle?.firstOrNull(),
        pdfUrl = null,

        concepts = emptyList(),

        citedByCount = null,
        referenceCount = null,
        references = emptyList(),

        extractedKeywords = null,
        coreSummary = null,
        applications = null,
        technicalContributions = null,
        category = null,

        trendScore = null,
        impactScore = null
    )
}

