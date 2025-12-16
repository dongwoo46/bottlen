package com.bottlen.bottlen_webflux.paper.client

import com.bottlen.bottlen_webflux.paper.domain.Paper
import com.bottlen.bottlen_webflux.paper.dto.FetchRequest
import com.bottlen.bottlen_webflux.paper.dto.FetchResponse

interface PaperClient {
    // 탐색용 (여러 개)
    suspend fun fetch(request: FetchRequest): FetchResponse

    // 식별자 기반 단건 조회
    suspend fun fetchByDoi(doi: String): Paper?
}
