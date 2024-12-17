package com.modsen.metadatafilesservice.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ZookeeperFileMetadata @JsonCreator constructor(
    @JsonProperty("fileUUID") var fileUUID: String = "",
    @JsonProperty("topic") var topic: String = "",
    @JsonProperty("partition") var partition: Int = 0,
    @JsonProperty("owner") var owner: String = "",
    @JsonProperty("fileName") var fileName: String = "",
    @JsonProperty("fileSize") var fileSize: Long = 0L,
    @JsonProperty("totalRecords") var totalRecords: Int = 0,
    @JsonProperty("mimeType") var mimeType: String = "",
    @JsonProperty("dirPath") var dirPath: String = "",
    @JsonProperty("metadataStatus") var metadataStatus: MetadataStatus = MetadataStatus.IN_PROGRESS,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("createdAt") var createdAt: String = LocalDateTime.now().toString(),

    @JsonProperty("zooFileName") var zooFileName: String = ""
)
