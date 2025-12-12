package com.bottlen.bottlen_webflux.disclosure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DartListItem(
        val corp_code: String?,   // 회사 코드
        val corp_name: String?,   // 회사명
        val stock_code: String?,  // 종목코드 (있을 때만)
        val corp_cls: String?,    // 상장/코스닥/기타 구분
        val report_nm: String?,   // 공시 제목
        val rcept_no: String?,    // ★ 공시 번호 (document.json에 사용)
        val flr_nm: String?,      // 제출자명
        val rcept_dt: String?,    // 공시일자 yyyyMMdd
        val rm: String?           // 비고
)