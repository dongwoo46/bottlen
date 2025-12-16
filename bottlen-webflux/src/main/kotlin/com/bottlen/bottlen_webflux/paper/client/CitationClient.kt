package com.bottlen.bottlen_webflux.paper.client

import com.bottlen.bottlen_webflux.paper.dto.opencitations.OpenCitationsResponse

interface CitationClient {
    suspend fun fetchCitations(doi: String): List<OpenCitationsResponse>
    suspend fun fetchReferences(doi: String): List<OpenCitationsResponse>
}
