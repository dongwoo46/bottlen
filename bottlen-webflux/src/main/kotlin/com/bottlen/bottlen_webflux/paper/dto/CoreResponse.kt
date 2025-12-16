package com.bottlen.bottlen_webflux.paper.dto.core

data class CoreResponse(
        val results: List<CoreWork>?
)

data class CoreWork(
        val id: String?,
        val title: String?,
        val abstractText: String?,
        val fullText: String?,
        val authors: List<String>?,
        val year: Int?,
        val doi: String?,
        val downloadUrl: String?
)
