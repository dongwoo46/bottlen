package com.bottlen.bottlen_webflux.paper.dto.opencitations

data class OpenCitationsResponse(
        val oci: String?,
        val citing: String?,   // 인용한 논문 DOI
        val cited: String?,    // 인용당한 논문 DOI
        val creation: String?,// 인용 발생 날짜
        val timespan: String? // cited → citing 시간 간격
)
