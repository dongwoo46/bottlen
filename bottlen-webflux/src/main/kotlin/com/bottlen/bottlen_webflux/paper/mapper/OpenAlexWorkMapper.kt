package com.bottlen.bottlen_webflux.paper.mapper

import com.bottlen.bottlen_webflux.paper.domain.*
import com.bottlen.bottlen_webflux.paper.dto.openalex.*

fun OpenAlexWork.toPaper(): Paper? {
    val paperId = id ?: return null

    return Paper(
            id = paperId,
            doi = doi,
            url = id,
            title = title,
            abstract = abstractInvertedIndex?.toPlainText(),
            year = publicationYear,
            authors = authorships
                    ?.mapNotNull { it.author?.display_name }
                    ?: emptyList(),
            venue = hostVenue?.display_name,
            pdfUrl = null, // OpenAlex는 PDF 직접 제공 안 함
            concepts = concepts
                    ?.mapNotNull {
                        if (it.id == null || it.display_name == null || it.score == null) null
                        else PaperConcept(it.id, it.display_name, it.score)
                    }
                    ?: emptyList(),
            citedByCount = citedByCount,
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

/**
 * OpenAlex abstract_inverted_index → 일반 텍스트 변환
 */
fun Map<String, List<Int>>.toPlainText(): String {
    val positions = mutableMapOf<Int, String>()
    forEach { (word, indices) ->
        indices.forEach { idx -> positions[idx] = word }
    }
    return positions.toSortedMap().values.joinToString(" ")
}
