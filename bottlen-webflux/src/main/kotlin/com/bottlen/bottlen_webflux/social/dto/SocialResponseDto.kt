package com.bottlen.bottlen_webflux.social.dto

/**
 * 외부 API 응답(JSON)을 SocialDto 리스트로 변환하기 위한 공통 규약
 */
interface SocialResponseDto {
    fun toSocialDtoList(): List<SocialDto>
}