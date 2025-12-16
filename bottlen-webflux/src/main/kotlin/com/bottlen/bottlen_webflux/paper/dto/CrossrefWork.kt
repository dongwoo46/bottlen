package com.bottlen.bottlen_webflux.paper.dto.crossref

import com.fasterxml.jackson.annotation.JsonProperty

data class CrossrefWork(

    @JsonProperty("DOI")
    val doi: String?,

    val title: List<String>?,

    val author: List<Author>?,

    val issued: Issued?,

    @JsonProperty("container-title")
    val containerTitle: List<String>?,

    val URL: String?,

    @JsonProperty("abstract")
    val abstract: String?
) {
    data class Author(
        val given: String?,
        val family: String?
    )

    data class Issued(
        @JsonProperty("date-parts")
        val dateParts: List<List<Int>>?
    )
}
