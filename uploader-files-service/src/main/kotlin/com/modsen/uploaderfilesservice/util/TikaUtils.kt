package com.modsen.uploaderfilesservice.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.tika.Tika

suspend fun detectMimeType(chunk: ByteArray): String = withContext(Dispatchers.IO) {
    val tika = Tika()
    tika.detect(chunk)
}