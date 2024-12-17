package com.modsen.uploaderfilesservice.service

import com.modsen.uploaderfilesservice.model.FileMetadata
import com.modsen.uploaderfilesservice.model.FolderResponse
import com.modsen.uploaderfilesservice.model.LimitedFileMetadata
import com.modsen.uploaderfilesservice.model.ZookeeperFileMetadata
import com.modsen.uploaderfilesservice.repository.MetadataStorageRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Service
class ZookeeperFileMetadataStorage(
    private val webClient: WebClient
) : MetadataStorageRepository {

    override suspend fun storeMetadata(metadata: FileMetadata) {
        val zookeeperFileMetadata = ZookeeperFileMetadata().apply {
            this.fileUUID = metadata.fileUUID
            this.topic = metadata.topic
            this.partition = metadata.partition
            this.owner = metadata.owner
            this.fileName = metadata.fileName
            this.fileSize = metadata.fileSize
            this.totalRecords = metadata.totalRecords
            this.mimeType = metadata.mimeType
            this.dirPath = metadata.dirPath
            this.zooFileName = buildZookeeperFileName(metadata.fileName, metadata.fileUUID)
            this.metadataStatus = metadata.metadataStatus
        }
        webClient.post()
            .uri("/file")
            .bodyValue(zookeeperFileMetadata)
            .retrieve()
            .onStatus({ status -> status.isError }) { response ->
                response.bodyToMono(String::class.java).flatMap { body ->
                    println(body)
                    Mono.error(
                        ResponseStatusException(
                            response.statusCode(),
                            body
                        )
                    )
                }
            }
            .bodyToMono(Void::class.java)
            .subscribe()
    }

    override suspend fun deleteFileMetadata(limitedFileMetadata: LimitedFileMetadata) {
        webClient.delete()
            .uri { uriBuilder ->
                uriBuilder.path("/file")
                    .queryParam("owner", limitedFileMetadata.owner)
                    .queryParam("path", limitedFileMetadata.dirPath)
                    .queryParam("fileName", limitedFileMetadata.fileName)
                    .queryParam("fileUUID", limitedFileMetadata.fileUUID)
                    .build()
            }
            .retrieve()
            .onStatus({ status -> status.isError }) { response ->
                response.bodyToMono(String::class.java).flatMap { body ->
                    Mono.error(
                        ResponseStatusException(
                            response.statusCode(),
                            body
                        )
                    )
                }
            }
            .bodyToMono(Void::class.java)
            .awaitSingleOrNull()
    }

    override suspend fun getFolderMetadata(owner: String, path: String) =
        webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/folder")
                    .queryParam("owner", owner)
                    .queryParam("path", path)
                    .build()
            }
            .retrieve()
            .onStatus({ status -> status.isError }) { response ->
                response.bodyToMono(String::class.java).flatMap { body ->
                    println("body $body")
                    Mono.error(
                        ResponseStatusException(
                            response.statusCode(),
                            body
                        )
                    )
                }
            }
            .bodyToMono(FolderResponse::class.java)
            .awaitSingle()

    override suspend fun deleteFolderMetadata(owner: String, path: String) {
        webClient.delete()
            .uri { uriBuilder ->
                uriBuilder.path("/folder")
                    .queryParam("owner", owner)
                    .queryParam("path", path)
                    .build()
            }
            .retrieve()
            .onStatus({ status -> status.isError }) { response ->
                response.bodyToMono(String::class.java).flatMap { body ->
                    println(body)
                    Mono.error(
                        ResponseStatusException(
                            response.statusCode(),
                            body
                        )
                    )
                }
            }
            .bodyToMono(Void::class.java)
            .awaitSingleOrNull()
    }

    private fun buildZookeeperFileName(fileName: String, fileUUID: String) =
        "file_${fileName}_$fileUUID"
}