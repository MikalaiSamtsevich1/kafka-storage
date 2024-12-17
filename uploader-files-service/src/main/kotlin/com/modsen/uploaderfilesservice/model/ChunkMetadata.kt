package com.modsen.uploaderfilesservice.model

data class ChunkMetadata(
    val owner: String,
    val fileName: String,
    val uuid: String,
    val position: Long,
    val totalSize: Long,
    val size: Int,
    val dirPath: String
) {

    fun isFirstChunk(): Boolean = position == 0L

    fun isLastChunk(): Boolean =
        position + size == totalSize

    fun generateKey(): String = "$owner$fileName$uuid"
}