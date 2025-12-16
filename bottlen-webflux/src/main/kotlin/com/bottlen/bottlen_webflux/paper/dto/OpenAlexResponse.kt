package com.bottlen.bottlen_webflux.paper.dto.openalex

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAlexResponse(
        val results: List<OpenAlexWork>?,
        val meta: OpenAlexMeta?
)

data class OpenAlexMeta(
        @JsonProperty("next_cursor")
        val nextCursor: String?
)

data class OpenAlexWork(
        val id: String?,
        val doi: String?,
        val title: String?,
        @JsonProperty("abstract_inverted_index")
        val abstractInvertedIndex: Map<String, List<Int>>?,
        @JsonProperty("publication_year")
        val publicationYear: Int?,
        val authorships: List<OpenAlexAuthorship>?,
        @JsonProperty("host_venue")
        val hostVenue: OpenAlexVenue?,
        val concepts: List<OpenAlexConcept>?,
        @JsonProperty("cited_by_count")
        val citedByCount: Int?
)

data class OpenAlexAuthorship(
        val author: OpenAlexAuthor?
)

data class OpenAlexAuthor(
        val display_name: String?
)

data class OpenAlexVenue(
        val display_name: String?
)

data class OpenAlexConcept(
        val id: String?,
        val display_name: String?,
        val score: Double?
)
