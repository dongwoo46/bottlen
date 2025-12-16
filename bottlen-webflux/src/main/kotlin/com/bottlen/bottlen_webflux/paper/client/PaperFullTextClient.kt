package com.bottlen.bottlen_webflux.paper.client

import com.bottlen.bottlen_webflux.paper.domain.PaperDocument

interface PaperFullTextClient {
    suspend fun fetchByDoi(doi: String): PaperDocument?
}
