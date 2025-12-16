package com.bottlen.bottlen_webflux.paper.dto

import com.bottlen.bottlen_webflux.paper.domain.Paper

data class FetchResponse(
        val papers: List<Paper>,      // 표준화 Paper 엔티티
        val nextCursor: String? = null,
        val nextPage: Int? = null
)
