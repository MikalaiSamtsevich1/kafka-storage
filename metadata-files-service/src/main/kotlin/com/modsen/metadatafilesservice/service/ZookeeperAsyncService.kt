package com.modsen.metadatafilesservice.service

import com.modsen.metadatafilesservice.exception.ZookeeperServiceException
import com.modsen.metadatafilesservice.model.FolderResponse
import com.modsen.metadatafilesservice.model.ZookeeperFileMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.x.async.AsyncCuratorFramework
import org.apache.curator.x.async.modeled.JacksonModelSerializer
import org.apache.curator.x.async.modeled.ModelSpec
import org.apache.curator.x.async.modeled.ModeledFramework
import org.apache.curator.x.async.modeled.ZPath
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.util.concurrent.CompletionStage

@Service
class ZookeeperAsyncService(
    zooClient: CuratorFramework,
    @Value("\${zookeeper.base-path}") private val zookeeperBasePath: String,
) {

    private val zooAsyncClient = AsyncCuratorFramework.wrap(zooClient)
    private val logger: Logger = LoggerFactory.getLogger(ZookeeperAsyncService::class.java)

    suspend fun saveMetadata(metadata: ZookeeperFileMetadata): String {
        val dirPath =
            "$zookeeperBasePath/${metadata.owner}/${metadata.dirPath}/${metadata.zooFileName}"
        val modeledClient = createModeledClient(dirPath)
        logger.info(dirPath)
        return modeledClient.set(metadata).await()
    }

    suspend fun getMetadata(
        owner: String,
        dirPath: String,
        fileName: String,
        fileUUID: String
    ): ZookeeperFileMetadata {
        val zookeeperFileName = buildZookeeperFileName(fileName, fileUUID)
        val filePath = buildFilePath(owner, dirPath, zookeeperFileName)
        return fetchMetadata(filePath)
    }

    fun buildZookeeperFileName(fileName: String, fileUUID: String) =
        "file_${fileName}_$fileUUID"

    suspend fun fetchMetadata(filePath: String): ZookeeperFileMetadata {
        logger.debug("received file path: $filePath")
        return if (checkExists(filePath)) {
            val modeledClient = createModeledClient(filePath)
            modeledClient.read().exceptionally { ex ->
                logAndThrow("Failed to read metadata for path $filePath", ex)
            }.await()
        } else {
            logAndThrow("File not found at path $filePath", FileNotFoundException(filePath))
        }
    }

    suspend fun getFolderMetadata(ownerName: String, path: String): FolderResponse = coroutineScope {
        val children = getChildren(buildFolderPath(ownerName, path))
        val filesMetadata = children.filter { it.startsWith("file_") }.map { child ->
            async(Dispatchers.IO) {
                fetchMetadata(buildFilePath(ownerName, path, child))
            }
        }.awaitAll()

        val folders = children.filterNot { it.startsWith("file_") }

        FolderResponse(filesMetadata, folders)
    }

    fun deleteFileMetadata(ownerName: String, path: String, fileName: String, fileUUID: String) {
        val zookeeperFileName = buildZookeeperFileName(fileName, fileUUID)
        val path = buildFilePath(ownerName, path, zookeeperFileName)
        deleteMetadata(path)
    }

    suspend fun deleteFolderMetadata(ownerName: String, path: String) {
        val folderPath = buildFolderPath(ownerName, path)
        deleteZNodeRecursively(folderPath)
    }

    suspend fun getChildren(path: String): List<String> =
        if (checkExists(path)) {
            zooAsyncClient.children.forPath(path).await()
        } else {
            throw FileNotFoundException(path)
        }

    fun deleteMetadata(path: String): CompletionStage<Void> {
        return zooAsyncClient.delete()
            .forPath(path)
            .exceptionally { ex -> logAndThrow("Failed to delete metadata for path $path", ex) }
    }

    suspend fun deleteZNodeRecursively(path: String) {
        val children = getChildren(path)

        for (child in children) {
            val childPath = "$path/$child"
            deleteZNodeRecursively(childPath)
        }

        deleteMetadata(path)
    }

    suspend fun checkExists(path: String): Boolean {
        return zooAsyncClient.checkExists()
            .forPath(path)
            .toCompletableFuture()
            .thenApply { it != null }
            .await()
    }

    private fun buildFilePath(ownerName: String, path: String, fileName: String): String {
        if (path.isEmpty() || path == "/") {
            return "$zookeeperBasePath/$ownerName/$fileName"
        }
        return "$zookeeperBasePath/$ownerName$path/$fileName"
    }

    private fun buildFolderPath(ownerName: String, path: String): String {
        if (path.isEmpty() || path == "/") {
            return "$zookeeperBasePath/$ownerName"
        }
        return "$zookeeperBasePath/$ownerName$path"
    }

    fun createModeledClient(path: String): ModeledFramework<ZookeeperFileMetadata> {
        val modelSpec = ModelSpec.builder(
            ZPath.parseWithIds(path),
            JacksonModelSerializer.build(ZookeeperFileMetadata::class.java)
        ).build()
        return ModeledFramework.wrap(zooAsyncClient, modelSpec)
    }

    private fun <T> logAndThrow(message: String, ex: Throwable): T {
        logger.error(message, ex)
        throw ZookeeperServiceException(message, ex)
    }

    private fun <T> logAndThrow(message: String, ex: Exception): T {
        logger.error(message, ex)
        throw ex
    }
}
