package com.bottlen.bottlen_webflux.paper.dto

import com.bottlen.bottlen_webflux.paper.domain.PaperDocument

data class FetchResponse(
        val paperDocuments: List<PaperDocument>,     // 표준화 Paper 엔티티
        /**
         * Cursor-based pagination.
         * Used by OpenAlex, OpenCitations.
         * Mutually exclusive with nextPage.
         */
        val nextCursor: String?=null,

        /**
         * Page-based pagination.
         * Used by CORE, Crossref.
         * Mutually exclusive with nextCursor.
         */
        val nextPage: Int?=null
)
