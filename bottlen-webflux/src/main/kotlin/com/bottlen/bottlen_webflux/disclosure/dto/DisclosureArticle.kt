package com.bottlen.bottlen_webflux.disclosure.dto

data class DisclosureArticle(
        val id: String,
        val source: String,
        val corpId: String?,
        val formType: String,
        val title: String,
        val link: String,
        val published: String,
        val collectedAt: String,

        val highlights: List<String>? = null // 이걸 summary 대용으로 사용
)
