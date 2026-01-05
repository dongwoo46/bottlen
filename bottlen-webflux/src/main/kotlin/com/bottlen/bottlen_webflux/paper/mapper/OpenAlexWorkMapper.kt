package com.bottlen.bottlen_webflux.paper.mapper

import com.bottlen.bottlen_webflux.paper.domain.PaperDocument
import com.bottlen.bottlen_webflux.paper.domain.PaperSource
import com.bottlen.bottlen_webflux.paper.dto.openalex.OpenAlexWork

fun OpenAlexWork.toPaperDocument(): PaperDocument? {
    val workId = id ?: return null
    val titleValue = title ?: return null

    return PaperDocument(
        id = workId,
        title = titleValue,
        abstractText = abstractInvertedIndex?.toPlainText(),
        fullText = null,
        doi = doi,
        downloadUrl = null,
        source = PaperSource.OPENALEX
    )
}

/**
 * OpenAlex abstract_inverted_index → plain text
 */
fun Map<String, List<Int>>.toPlainText(): String {
    val positions = mutableMapOf<Int, String>()
    forEach { (word, indices) ->
        indices.forEach { idx -> positions[idx] = word }
    }
    return positions.toSortedMap().values.joinToString(" ")
}
