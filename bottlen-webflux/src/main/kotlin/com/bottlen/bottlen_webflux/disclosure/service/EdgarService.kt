package com.bottlen.bottlen_webflux.disclosure.service

import com.bottlen.bottlen_webflux.disclosure.client.EdgarClient
import com.bottlen.bottlen_webflux.disclosure.dto.DisclosureArticle
import com.bottlen.bottlen_webflux.disclosure.mapper.toEntity
import com.bottlen.bottlen_webflux.disclosure.repository.DisclosureRepository
import com.bottlen.bottlen_webflux.infra.redis.BloomFilterClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import org.springframework.stereotype.Service

@Service
class EdgarService(
        private val edgarClient: EdgarClient,
        private val disclosureRepository: DisclosureRepository,
        private val bloom: BloomFilterClient
) {

    private val filterName = "BF:disclosure"

    /**
     * EDGAR RSS → Flow<DisclosureArticle> 형태로 수집하여
     * 1) BloomFilter로 중복 차단
     * 2) Entity 변환하여 DB 저장
     * 3) Flow 로 신규 데이터만 스트리밍 반환
     */
    suspend fun collect(url: String): Flow<DisclosureArticle> {

        return edgarClient.fetch(url)
                .filter { article ->
                    // BloomFilter 중복 체크
                    val seen = bloom.exists(filterName, article.id)
                    !seen
                }
                .onEach { article ->
                    // BloomFilter 등록
                    bloom.add(filterName, article.id)

                    // DTO → Entity 변환 후 저장
                    val entity = article.toEntity()
                    disclosureRepository.save(entity)
                }
    }
}
