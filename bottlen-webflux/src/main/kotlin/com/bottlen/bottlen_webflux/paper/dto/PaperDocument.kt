package com.bottlen.bottlen_webflux.paper.domain

enum class PaperSource {
    CORE,
    OPENALEX,
    CROSSREF,
    OPENCITATIONS
}

data class PaperDocument(
    val id: String,
    val title: String?,
    val abstractText: String?,
    val fullText: String?,
    val doi: String?,
    val downloadUrl: String? = null,
    val source: PaperSource
)
