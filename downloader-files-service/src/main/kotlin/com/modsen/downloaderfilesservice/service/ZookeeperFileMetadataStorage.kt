package com.modsen.downloaderfilesservice.service

import com.modsen.downloaderfilesservice.model.LimitedFileMetadata
import com.modsen.downloaderfilesservice.model.ZookeeperFileMetadata
import com.modsen.downloaderfilesservice.repository.FileMetadataStorage
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ZookeeperFileMetadataStorage(
    private val webClient: WebClient
) : FileMetadataStorage {


    override suspend fun getMetadata(limitedFileMetadata: LimitedFileMetadata) =
        webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/file")
                    .queryParam("owner", limitedFileMetadata.owner)
                    .queryParam("dirPath", limitedFileMetadata.dirPath)
                    .queryParam("fileName", limitedFileMetadata.fileName)
                    .queryParam("fileUUID", limitedFileMetadata.fileUUID)
                    .build()
            }
            .retrieve()
            .bodyToMono(ZookeeperFileMetadata::class.java)
            .awaitSingle()
}