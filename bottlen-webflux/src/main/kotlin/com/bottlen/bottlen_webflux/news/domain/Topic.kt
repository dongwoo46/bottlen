package com.bottlen.bottlen_webflux.news.domain

enum class Topic(val code: String) {

    // ── RSS / Media categories ──
    MAIN("main"),
    SCIENCE("science"),
    LAW("law"),
    FEATURES("features"),
    BUSINESS("business"),
    CARS("cars"),

    // ── Technology / Industry ──
    TECHNOLOGY("tech"),
    AI("ai"),
    ENERGY("energy"),

    // ── Economy / Finance ──
    FINANCE("finance"),
    ECONOMY("economy"),

    // ── Fallback ──
    UNKNOWN("unknown");

    companion object {
        fun from(code: String?): Topic =
            entries.firstOrNull {
                it.code.equals(code, ignoreCase = true)
            } ?: UNKNOWN
    }
}
