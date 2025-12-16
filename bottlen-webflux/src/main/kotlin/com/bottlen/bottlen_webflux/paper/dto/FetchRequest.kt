package com.bottlen.bottlen_webflux.paper.dto

import java.time.LocalDate

data class FetchRequest(
        val keyword: String? = null,           // 검색어(OpenAlex search, CORE query, Crossref query)
        val fromDate: LocalDate? = null,       // 날짜 필터(OpenAlex, Crossref)
        val toDate: LocalDate? = null,
        val cursor: String? = null,            // OpenAlex / Crossref 커서
        val page: Int? = null,                 // CORE는 cursor 없음 → page 기반
        val limit: Int = 100,                  // 공통 제한
        val sort: String? = null               // 정렬 기준
)
