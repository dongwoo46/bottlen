package com.bottlen.bottlen_webflux.disclosure.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("disclosure_article")
data class DisclosureEntity(
        @Id
        val id: String,                // DTO.id 그대로 PK로 사용

        val source: String,
        val corpId: String?,
        val formType: String,
        val title: String,
        val link: String,

        val published: String,         // 문자열 그대로 저장
        val collectedAt: String,       // 문자열 그대로 저장

        val highlights: String?        // JSON 문자열 또는 null
)
