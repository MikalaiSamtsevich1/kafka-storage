package com.modsen.uploaderfilesservice.repository

import com.modsen.uploaderfilesservice.model.FileMetadata
import com.modsen.uploaderfilesservice.model.FolderResponse
import com.modsen.uploaderfilesservice.model.LimitedFileMetadata

interface MetadataStorageRepository {
    suspend fun storeMetadata(metadata: FileMetadata)
    suspend fun deleteFileMetadata(limitedFileMetadata: LimitedFileMetadata)
    suspend fun getFolderMetadata(owner: String, path: String): FolderResponse
    suspend fun deleteFolderMetadata(owner: String, path: String)
}