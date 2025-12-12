package com.bottlen.bottlen_webflux.disclosure.service

import com.bottlen.bottlen_webflux.disclosure.client.DartClient
import com.bottlen.bottlen_webflux.disclosure.entity.DisclosureEntity
import com.bottlen.bottlen_webflux.disclosure.mapper.toEntity
import com.bottlen.bottlen_webflux.disclosure.repository.DisclosureRepository
import com.bottlen.bottlen_webflux.infra.redis.BloomFilterClient
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class DartService(
        private val dartClient: DartClient,
        private val disclosureRepository: DisclosureRepository,
        private val bloom: BloomFilterClient
) {

    private val filterName = "BF:dart"
    private val fmt = DateTimeFormatter.ofPattern("yyyyMMdd")

    /**
     * 최근 7일간의 DART 공시 수집 + BloomFilter 중복 제거
     */
    suspend fun collectLatest(): List<DisclosureEntity> {

        val today = LocalDate.now().format(fmt)
        val weekAgo = LocalDate.now().minusDays(7).format(fmt)

        val flow = dartClient.fetchList(
                startDate = weekAgo,
                endDate = today
        )

        val savedList = mutableListOf<DisclosureEntity>()

        flow.collect { article ->
            // BloomFilter 중복 체크
            val exists = bloom.exists(filterName, article.id)
            if (exists) return@collect

            // BloomFilter에 등록
            bloom.add(filterName, article.id)

            // DB 저장
            val saved = disclosureRepository.save(article.toEntity())
            savedList.add(saved)
        }

        return savedList
    }
}
