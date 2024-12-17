package com.modsen.downloaderfilesservice.repository

import com.modsen.downloaderfilesservice.model.FileMetadata
import com.modsen.downloaderfilesservice.model.LimitedFileMetadata

interface FileMetadataStorage {
    suspend fun getMetadata(limitedFileMetadata: LimitedFileMetadata): FileMetadata
}