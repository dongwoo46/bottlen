package com.bottlen.bottlen_webflux.paper.domain

data class Paper(
        val id: String,
        val doi: String? = null,
        val url: String? = null,

        // 기본 정보
        val title: String,
        val abstract: String?,
        val year: Int? = null,
        val authors: List<String> = emptyList(),
        val venue: String? = null,
        val pdfUrl: String? = null,

        // 기술/분야 정보
        val concepts: List<PaperConcept> = emptyList(),

        // 인용 정보
        val citedByCount: Int? = null,
        val referenceCount: Int? = null,
        val references: List<String> = emptyList(),

        // NLP/LLM 분석 결과
        val extractedKeywords: List<String>? = null,
        val coreSummary: String? = null,
        val applications: List<String>? = null,
        val technicalContributions: String? = null,
        val category: String? = null,

        // 점수 정보
        val trendScore: Double? = null,
        val impactScore: Double? = null,
)
