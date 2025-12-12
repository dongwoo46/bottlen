package com.bottlen.bottlen_webflux.disclosure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DartListResponse(
        val status: String?,
        val message: String?,
        val page_no: Int?,
        val page_count: Int?,
        val total_page: Int?,
        val total_count: Int?,
        val list: List<DartListItem>?
)