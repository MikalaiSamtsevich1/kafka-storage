package com.modsen.metadatafilesservice.controller

import com.modsen.metadatafilesservice.model.ZookeeperFileMetadata
import com.modsen.metadatafilesservice.service.ZookeeperAsyncService
import kotlinx.coroutines.coroutineScope
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class FileStorageController(
    private val zoo: ZookeeperAsyncService,
) {

    @GetMapping("/folder")
    suspend fun getFolder(
        @RequestParam owner: String,
        @RequestParam path: String
    ) = coroutineScope {
        ResponseEntity.ok(zoo.getFolderMetadata(owner, path))
    }

    @GetMapping("/file")
    suspend fun getFile(
        @RequestParam owner: String,
        @RequestParam dirPath: String,
        @RequestParam fileName: String,
        @RequestParam fileUUID: String
    ) = coroutineScope {
        ResponseEntity.ok(zoo.getMetadata(owner, dirPath, fileName, fileUUID))
    }

    @PostMapping("/file")
    suspend fun saveFile(@RequestBody zookeeperFileMetadata: ZookeeperFileMetadata) = coroutineScope {
        ResponseEntity.ok(zoo.saveMetadata(zookeeperFileMetadata))
    }

    @DeleteMapping("/file")
    suspend fun deleteFile(
        @RequestParam owner: String,
        @RequestParam fileName: String,
        @RequestParam fileUUID: String,
        @RequestParam path: String
    ) = coroutineScope {
        ResponseEntity.ok(zoo.deleteFileMetadata(owner, path, fileName, fileUUID))
    }

    @DeleteMapping("/folder")
    suspend fun deleteFolder(
        @RequestParam owner: String,
        @RequestParam path: String
    ) = coroutineScope {
        ResponseEntity.ok(zoo.deleteFolderMetadata(owner, path))
    }

}
