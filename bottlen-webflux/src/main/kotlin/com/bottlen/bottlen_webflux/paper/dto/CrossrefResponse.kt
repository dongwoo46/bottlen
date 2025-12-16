package com.bottlen.bottlen_webflux.paper.dto.crossref

import com.fasterxml.jackson.annotation.JsonProperty

data class CrossrefResponse(
        val message: Message?
) {
        data class Message(
                val items: List<CrossrefWork>?,

                @JsonProperty("total-results")
                val totalResults: Int?,

                @JsonProperty("next-cursor")
                val nextCursor: String?
        )
}
