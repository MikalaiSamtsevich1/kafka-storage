package com.modsen.uploaderfilesservice.model

import java.time.LocalDateTime

data class FileMetadataDefault(
    override var fileUUID: String = "",
    override var topic: String = "",
    override var partition: Int = 0,
    override var owner: String = "",
    override var fileName: String = "",
    override var fileSize: Long = 0L,
    override var totalRecords: Int = 0,
    override var mimeType: String = "",
    override var dirPath: String = "",
    override var metadataStatus: MetadataStatus = MetadataStatus.IN_PROGRESS,
    override var createdAt: String= LocalDateTime.now().toString(),
) : FileMetadata