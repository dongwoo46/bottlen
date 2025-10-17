package com.bottlen.bottlen_webflux.news.domain

/**
 * 뉴스 API별 카테고리 매핑
 * - Guardian: section 기반
 * - NYT: section/category 기반
 * - NewsCatcher / NewsData: topic/category 기반
 */
object NewsCategoryGroup {

    val GUARDIAN = listOf(
            NewsCategory.BUSINESS,
            NewsCategory.AI,
            NewsCategory.DEFENSE,
            NewsCategory.TECHNOLOGY,
            NewsCategory.WORLD,
            NewsCategory.SCIENCE
    )

    val NYT = listOf(
            NewsCategory.BUSINESS,     // 기업 실적, 산업, M&A
            NewsCategory.TECHNOLOGY,   // 테크, AI, 반도체
            NewsCategory.SCIENCE,      // 혁신/에너지/연구
            NewsCategory.POLITICS,     // 정책/규제/금리
            NewsCategory.WORLD,        // 국제 경제/전쟁/환율
            NewsCategory.US,           // 미국 내 정책/금융 동향
            NewsCategory.HEALTH,       // 헬스케어/바이오 산업
            NewsCategory.REALESTATE    // 부동산 경기
    )


    val NEWS_CATCHER = listOf(
            NewsCategory.BUSINESS,
            NewsCategory.ECONOMY,
            NewsCategory.INNOVATION,
            NewsCategory.CRYPTO,
            NewsCategory.STARTUPS
    )

    val NEWS_DATA = listOf(
            NewsCategory.WORLD,
            NewsCategory.SCIENCE,
            NewsCategory.TECHNOLOGY,
            NewsCategory.ENVIRONMENT
    )
}
