package com.modsen.downloaderfilesservice.model

interface LimitedFileMetadata {
    val owner: String
    val dirPath: String
    val fileName: String
    val fileUUID: String
}