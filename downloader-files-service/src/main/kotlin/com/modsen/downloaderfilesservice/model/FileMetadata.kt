package com.modsen.downloaderfilesservice.model

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
}