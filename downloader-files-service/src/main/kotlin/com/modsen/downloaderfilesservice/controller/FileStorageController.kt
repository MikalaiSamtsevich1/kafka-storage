package com.modsen.downloaderfilesservice.controller

import com.modsen.downloaderfilesservice.model.LimitedFileMetadataDefault
import com.modsen.downloaderfilesservice.service.KafkaStorageAsyncService
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class FileStorageController(
    private val kafkaStorageService: KafkaStorageAsyncService,
) {

    @GetMapping("/stream/download")
    suspend fun streamFile(
        @RequestParam owner: String,
        @RequestParam path: String,
        @RequestParam fileName: String,
        @RequestParam fileUUID: String,
    ): ResponseEntity<Flow<ByteArray>> {

        val limitedFileMetadata = LimitedFileMetadataDefault(
            owner = owner,
            dirPath = path,
            fileName = fileName,
            fileUUID = fileUUID
        )

        val data = kafkaStorageService.receiveChunks(limitedFileMetadata)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${fileName}\"")
            .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
            .contentType(MediaType.APPLICATION_OCTET_STREAM).body(data)
    }

}