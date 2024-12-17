package com.modsen.metadatafilesservice.model

data class FolderResponse(
    val files: List<ZookeeperFileMetadata>,
    val folders: List<String>
)
