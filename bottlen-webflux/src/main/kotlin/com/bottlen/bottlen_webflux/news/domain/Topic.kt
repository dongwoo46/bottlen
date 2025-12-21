package com.bottlen.bottlen_webflux.news.domain

enum class Topic(val code: String) {
    TECHNOLOGY("tech"),
    AI("ai"),
    FINANCE("finance"),
    ECONOMY("economy"),
    ENERGY("energy");

    companion object {
        fun fromCode(code: String): Topic =
            values().first { it.code == code }
    }
}
