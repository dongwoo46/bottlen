package com.bottlen.bottlen_webflux.disclosure.mapper

import com.bottlen.bottlen_webflux.disclosure.dto.DisclosureArticle
import com.bottlen.bottlen_webflux.disclosure.entity.DisclosureEntity

/**
 * DisclosureArticle → DisclosureEntity 변환 확장 함수
 */
fun DisclosureArticle.toEntity(): DisclosureEntity {
    return DisclosureEntity(
            id = this.id,
            source = this.source,
            corpId = this.corpId,
            formType = this.formType,
            title = this.title,
            link = this.link,
            published = this.published,
            collectedAt = this.collectedAt,
            highlights = this.highlights?.joinToString("||")
    )
}
