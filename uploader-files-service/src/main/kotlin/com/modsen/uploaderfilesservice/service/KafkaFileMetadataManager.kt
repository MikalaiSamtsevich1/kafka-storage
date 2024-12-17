package com.modsen.uploaderfilesservice.service

import com.modsen.uploaderfilesservice.exception.MetadataHandleException
import com.modsen.uploaderfilesservice.model.ChunkMetadata
import com.modsen.uploaderfilesservice.model.FileMetadataDefault
import com.modsen.uploaderfilesservice.model.LimitedFileMetadataDefault
import com.modsen.uploaderfilesservice.model.MetadataStatus
import com.modsen.uploaderfilesservice.repository.MetadataStorageRepository
import com.modsen.uploaderfilesservice.util.detectMimeType
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.producer.RecordMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Service
class KafkaFileMetadataManager(
    private val metadataRepository: MetadataStorageRepository,
    @Value("\${kafka.message-size}") private val chunkSize: Int,
    private val adminClient: AdminClient,
) {
    private val logger: Logger = LoggerFactory.getLogger(KafkaFileMetadataManager::class.java)

    private val fileMetadataMap = ConcurrentHashMap<String, FileMetadataDefault>()


    @Scheduled(fixedRateString = "30", timeUnit = TimeUnit.SECONDS)
    @SchedulerLock(name = "RoutineScheduler.scheduledTask", lockAtLeastFor = "PT15S", lockAtMostFor = "PT30S")
    suspend fun removeExpiredMetadata() {
        val timeToClean = LocalDateTime.now().minusSeconds(30)
        logger.info("started cleanup task")
        fileMetadataMap.forEach { (key, metadata) ->
            if (LocalDateTime.parse(metadata.createdAt).isBefore(timeToClean)) {
                fileMetadataMap.remove(key)
            }
        }
    }

    suspend fun handleMetadata(
        topic: String,
        chunkMetadata: ChunkMetadata,
        recordMetadata: RecordMetadata,
        chunk: ByteArray
    ) {
        when {
            chunkMetadata.isFirstChunk() -> {
                handleFirstChunk(
                    topic,
                    chunk,
                    recordMetadata,
                    chunkMetadata
                )
            }

            chunkMetadata.isLastChunk() -> {
                handleLastChunk(chunkMetadata)
            }

            fileMetadataMap[chunkMetadata.generateKey()] == null -> {
                val topic = "${chunkMetadata.owner}-${chunkMetadata.fileName}-${chunkMetadata.uuid}"
                adminClient.deleteTopics(listOf(topic))
                logger.info("Removing topic: $topic")
                throw MetadataHandleException("File ${chunkMetadata.fileName} has been deleted")
            }
        }
    }

    private suspend fun handleFirstChunk(
        topic: String,
        chunk: ByteArray,
        metadata: RecordMetadata,
        chunkMetadata: ChunkMetadata,
    ) {
        val fileMetadata =
            getOrCreateMetadata(chunkMetadata.generateKey())
        val mimeType = detectMimeType(chunk)
        fileMetadata.apply {
            this.fileUUID = chunkMetadata.uuid
            this.topic = topic
            this.partition = metadata.partition()
            this.owner = chunkMetadata.owner
            this.fileName = chunkMetadata.fileName
            this.fileSize = chunkMetadata.totalSize
            this.totalRecords = (chunkMetadata.totalSize / (chunkSize)).toInt() + 1
            this.mimeType = mimeType
            this.dirPath = chunkMetadata.dirPath
            this.metadataStatus = MetadataStatus.IN_PROGRESS
            this.createdAt = LocalDateTime.now().toString()
        }
        logger.debug("metadata initialized: ${fileMetadata.fileUUID}")
    }

    private fun getOrCreateMetadata(key: String): FileMetadataDefault {
        return fileMetadataMap.computeIfAbsent(key) { FileMetadataDefault() }
    }

    private suspend fun handleLastChunk(chunkMetadata: ChunkMetadata) {
        fileMetadataMap[chunkMetadata.generateKey()]?.let { metadata ->
            fileMetadataMap.remove(chunkMetadata.generateKey())
            metadata.metadataStatus = MetadataStatus.COMPLETED
            metadataRepository.storeMetadata(metadata)
        }
    }

    suspend fun deleteFile(owner: String, path: String, fileName: String, uuid: String) {
        val limitedFileMetadata = LimitedFileMetadataDefault(
            dirPath = path,
            fileName = fileName,
            fileUUID = uuid,
            owner = owner
        )
        metadataRepository.deleteFileMetadata(limitedFileMetadata)
    }

    suspend fun getFolder(owner: String, path: String) =
        metadataRepository.getFolderMetadata(owner, path)

    suspend fun deleteFolder(owner: String, path: String) {
        metadataRepository.deleteFolderMetadata(owner, path)
    }


}