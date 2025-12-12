package com.bottlen.bottlen_webflux.disclosure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DartDocumentResponse(
        val status: String?,
        val message: String?,
        val list: List<DartDocumentItem>?
)