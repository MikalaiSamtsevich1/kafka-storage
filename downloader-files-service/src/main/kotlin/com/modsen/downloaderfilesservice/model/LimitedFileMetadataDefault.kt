package com.modsen.downloaderfilesservice.model

data class LimitedFileMetadataDefault(
    override var fileUUID: String = "",
    override var owner: String = "",
    override var fileName: String = "",
    override var dirPath: String = "",
) : LimitedFileMetadata

