package com.bottlen.bottlen_webflux.news.domain

/**
 * 전역 뉴스 카테고리 Enum
 * 각 API에서 공통적으로 사용 가능한 의미적 분류
 */
enum class NewsCategory(val label: String) {
    BUSINESS("business"),
    AI("ai"),
    DEFENSE("defense"),
    TECHNOLOGY("technology"),
    WORLD("world"),
    SCIENCE("science"),
    ECONOMY("economy"),
    INNOVATION("innovation"),
    CRYPTO("crypto"),
    STARTUPS("startups"),
    POLITICS("politics"),
    ENVIRONMENT("environment"),
    US("us"),
    HEALTH("health"),
    REALESTATE("realestate");

    companion object {
        fun fromLabel(label: String): NewsCategory? =
                entries.find { it.label.equals(label, ignoreCase = true) }
    }
}
