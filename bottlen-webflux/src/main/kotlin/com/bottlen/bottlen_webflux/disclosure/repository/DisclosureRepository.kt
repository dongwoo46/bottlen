package com.bottlen.bottlen_webflux.disclosure.repository

import com.bottlen.bottlen_webflux.disclosure.entity.DisclosureEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface DisclosureRepository : CoroutineCrudRepository<DisclosureEntity, String>
