package com.bottlen.bottlen_webflux.paper.client

import com.bottlen.bottlen_webflux.paper.dto.core.CoreWork

interface PaperFullTextClient {
    suspend fun fetchByDoi(doi: String): CoreWork?
}
