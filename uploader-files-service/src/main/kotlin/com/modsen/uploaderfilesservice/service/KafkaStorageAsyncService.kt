package com.modsen.uploaderfilesservice.service

import com.modsen.uploaderfilesservice.model.ChunkMetadata
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.DeleteTopicsResult
import org.springframework.stereotype.Service

@Service
class KafkaStorageAsyncService(
    private val kafkaSenderService: KafkaSenderService,
    private val kafkaFileMetadataService: KafkaFileMetadataManager,
    private val adminClient: AdminClient,
) {

    suspend fun uploadChunks(
        topic: String,
        chunk: ByteArray,
        chunkMetadata: ChunkMetadata
    ) {
        val recordMetadata = kafkaSenderService.sendChunk(topic, chunkMetadata, chunk)
        kafkaFileMetadataService.handleMetadata(topic, chunkMetadata, recordMetadata, chunk)
    }

    suspend fun deleteFile(owner: String, path: String, fileName: String, uuid: String): DeleteTopicsResult {
        val topic = "${owner}-${fileName}-${uuid}"
        val deleteTopicsResult = adminClient.deleteTopics(listOf(topic))
        kafkaFileMetadataService.deleteFile(owner = owner, path = path, fileName = fileName, uuid = uuid)
        return deleteTopicsResult
    }

    suspend fun deleteFolder(owner: String, path: String) {
        val folder = kafkaFileMetadataService.getFolder(owner = owner, path = path)
        println("folder: $folder")
        folder.files.forEach { file ->
            val topic = "${file.owner}-${file.fileName}-${file.fileUUID}"
            println("Deleting file: $file")
            println("Deleting topic: $topic")

            println(adminClient.deleteTopics(listOf(topic)).all())

            kafkaFileMetadataService.deleteFile(
                owner = file.owner,
                path = "/${file.dirPath}",
                fileName = file.fileName,
                uuid = file.fileUUID
            )
        }

        folder.folders.forEach { subfolder ->
            var subfolderPath =
                if (path == "/")
                    "$path$subfolder"
                else
                    "$path/$subfolder"
            println("Entering subfolder: $subfolderPath")
            deleteFolder(owner, subfolderPath)
        }

        kafkaFileMetadataService.deleteFolder(owner, path)
        println("Deleted folder: $path")
    }
}