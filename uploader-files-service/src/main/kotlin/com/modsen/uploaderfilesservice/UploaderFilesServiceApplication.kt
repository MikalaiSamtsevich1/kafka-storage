package com.modsen.uploaderfilesservice

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT60S")
@SpringBootApplication
class UploaderFilesServiceApplication

fun main(args: Array<String>) {
	runApplication<UploaderFilesServiceApplication>(*args)
}
