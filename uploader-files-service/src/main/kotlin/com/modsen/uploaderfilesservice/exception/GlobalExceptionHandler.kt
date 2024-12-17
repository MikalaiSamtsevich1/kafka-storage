package com.modsen.uploaderfilesservice.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ResponseStatusException

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException::class)
    suspend fun responseStatusException(e: ResponseStatusException): ResponseEntity<String> {
        return ResponseEntity.status(e.statusCode).body(e.message)
    }

    @ExceptionHandler(MetadataHandleException::class)
    suspend fun metadataHandleException(e: MetadataHandleException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.GONE).body(e.message)
    }

}