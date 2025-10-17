package com.bottlen.bottlen_webflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling  // ★ 이거 있어야 @Scheduled 작동
class BottlenWebfluxApplication

fun main(args: Array<String>) {
	runApplication<BottlenWebfluxApplication>(*args)
}
