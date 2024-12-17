package com.modsen.uploaderfilesservice.model

import java.time.LocalDateTime

interface FileMetadata {
    var fileUUID: String
    var topic: String
    var partition: Int
    var owner: String
    var fileName: String
    var fileSize: Long
    var totalRecords: Int
    var mimeType: String
    var dirPath: String
    var createdAt: String
    var metadataStatus: MetadataStatus
}