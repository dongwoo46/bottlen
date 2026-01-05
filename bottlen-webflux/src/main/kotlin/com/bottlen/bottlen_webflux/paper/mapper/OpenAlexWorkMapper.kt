package com.bottlen.bottlen_webflux.paper.mapper

import com.bottlen.bottlen_webflux.paper.domain.*
import com.bottlen.bottlen_webflux.paper.dto.openalex.*
fun OpenAlexWork.toPaper(): Paper? {
    val paperId = id ?: return null
    val titleValue = title ?: return null

    return Paper(
        id = paperId,
        doi = doi,
        url = id,

        title = titleValue,
        abstract = abstractInvertedIndex?.toPlainText(),
        year = publicationYear,

        authors = authorships
            ?.mapNotNull { it.author?.display_name }
            ?: emptyList(),

        venue = hostVenue?.display_name,
        pdfUrl = null,

        concepts = concepts
            ?.mapNotNull {
                val cid = it.id
                val name = it.display_name
                val score = it.score
                if (cid == null || name == null || score == null) null
                else PaperConcept(cid, name, score)
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
