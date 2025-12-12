package com.bottlen.bottlen_webflux.disclosure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DartDocumentItem(
        val rcept_no: String?,     // 공시 번호
        val corp_code: String?,    // 회사 코드
        val corp_name: String?,    // 회사명
        val report_nm: String?,    // 공시 제목
        val se: String?,           // 문서 구분(본문, 첨부문서, 별첨 등)
        val content: String?       // HTML/XML 본문
)
