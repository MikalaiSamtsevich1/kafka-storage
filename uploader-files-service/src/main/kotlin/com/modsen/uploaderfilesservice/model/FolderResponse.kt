package com.modsen.uploaderfilesservice.model

data class FolderResponse(
    val files: List<ZookeeperFileMetadata>,
    val folders: List<String>
)
