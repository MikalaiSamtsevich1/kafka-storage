package com.modsen.downloaderfilesservice.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ZookeeperFileMetadata @JsonCreator constructor(
    @JsonProperty("fileUUID") override var fileUUID: String = "",
    @JsonProperty("topic") override var topic: String = "",
    @JsonProperty("partition") override var partition: Int = 0,
    @JsonProperty("owner") override var owner: String = "",
    @JsonProperty("fileName") override var fileName: String = "",
    @JsonProperty("fileSize") override var fileSize: Long = 0L,
    @JsonProperty("totalRecords") override var totalRecords: Int = 0,
    @JsonProperty("mimeType") override var mimeType: String = "",
    @JsonProperty("dirPath") override var dirPath: String = "",
    @JsonProperty("zooFileName") var zooFileName: String = "", // Specific for zookeeper
) : FileMetadata