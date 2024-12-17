package com.modsen.uploaderfilesservice.model

interface LimitedFileMetadata {
    val owner: String
    val dirPath: String
    val fileName: String
    val fileUUID: String
}