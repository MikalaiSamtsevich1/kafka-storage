package com.modsen.uploaderfilesservice.controller

import com.modsen.uploaderfilesservice.model.ChunkMetadata
import com.modsen.uploaderfilesservice.service.KafkaStorageAsyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class KafkaFileUploaderController(
    private val kafkaStorageService: KafkaStorageAsyncService,
) {

    private val logger: Logger = LoggerFactory.getLogger(KafkaFileUploaderController::class.java)

    @PostMapping("/stream/upload")
    suspend fun uploadChunk(
        @RequestHeader("Content-Range") contentRange: String,
        @RequestParam owner: String,
        @RequestParam dirPath: String,
        @RequestParam fileName: String,
        @RequestParam uuid: String,
        @RequestBody chunk: ByteArray
    ): ResponseEntity<Any> {
        logger.info("content range: $contentRange")
        val range = contentRange.split("bytes ")[1].split("/")
        val positions = range[0].split("-")
        val chunkMetadata = ChunkMetadata(
            owner = owner,
            fileName = fileName,
            uuid = uuid,
            position = positions[0].toLong(),
            totalSize = range[1].toLong(),
            dirPath = dirPath,
            size = chunk.size
        )

        val topic = "${owner}-${fileName}-${uuid}"

        coroutineScope {
            launch(Dispatchers.IO) {
                kafkaStorageService.uploadChunks(
                    topic,
                    chunk,
                    chunkMetadata
                )
            }
        }

        return ResponseEntity.noContent().build<Any>()
    }

    @DeleteMapping("/file")
    suspend fun deleteFile(
        @RequestParam owner: String,
        @RequestParam fileName: String,
        @RequestParam fileUUID: String,
        @RequestParam path: String
    ) = coroutineScope {
        kafkaStorageService.deleteFile(owner, path, fileName, fileUUID)
    }

    @DeleteMapping("/folder")
    suspend fun deleteFolder(
        @RequestParam owner: String,
        @RequestParam path: String
    ) = coroutineScope {
        kafkaStorageService.deleteFolder(owner, path)
    }

}