package com.modsen.metadatafilesservice.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.FileNotFoundException

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(FileNotFoundException::class)
    suspend fun handleFileNotFoundException(e: FileNotFoundException): ResponseEntity<String> {
        println(e.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
    }

}