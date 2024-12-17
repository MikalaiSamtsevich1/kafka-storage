package com.modsen.uploaderfilesservice.service

import com.modsen.uploaderfilesservice.model.ChunkMetadata
import com.modsen.uploaderfilesservice.model.FileMetadataDefault
import com.modsen.uploaderfilesservice.model.FolderResponse
import com.modsen.uploaderfilesservice.model.LimitedFileMetadataDefault
import com.modsen.uploaderfilesservice.repository.MetadataStorageRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.producer.RecordMetadata
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class KafkaFileMetadataManagerTest {

    private val metadataRepository = mockk<MetadataStorageRepository>()
    private val chunkSize = 1024
    private lateinit var kafkaFileMetadataManager: KafkaFileMetadataManager
    private var adminClient:AdminClient = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        kafkaFileMetadataManager = KafkaFileMetadataManager(metadataRepository, chunkSize, adminClient)
    }

    @Test
    fun `handleMetadata should initialize file metadata for first chunk`() = runTest {
        // given
        val topic = "test-topic"
        val chunk = ByteArray(1024)
        val chunkMetadata = mockk<ChunkMetadata>()
        val recordMetadata = mockk<RecordMetadata>()
        val fileUUID = "file-uuid"
        val owner = "owner"
        val fileName = "file.txt"
        val totalSize = 5000L
        val dirPath = "/path/to/dir"

        every { chunkMetadata.isFirstChunk() } returns true
        every { chunkMetadata.isLastChunk() } returns false
        every { chunkMetadata.uuid } returns fileUUID
        every { chunkMetadata.owner } returns owner
        every { chunkMetadata.fileName } returns fileName
        every { chunkMetadata.totalSize } returns totalSize
        every { chunkMetadata.dirPath } returns dirPath
        every { chunkMetadata.generateKey() } returns "generated-key"
        every { recordMetadata.partition() } returns 1

        // when
        kafkaFileMetadataManager.handleMetadata(topic, chunkMetadata, recordMetadata, chunk)

        // then
        coVerify(exactly = 0) { metadataRepository.storeMetadata(any()) }
    }


    @Test
    fun `handleMetadata should store metadata and remove it from map for last chunk`() = runTest {
        // given
        val topic = "test-topic"
        val chunk = ByteArray(1024)
        val chunkMetadata = mockk<ChunkMetadata>()
        val recordMetadata = mockk<RecordMetadata>()
        val fileUUID = "file-uuid"
        val owner = "owner"
        val fileName = "file.txt"
        val totalSize = 5000L
        val dirPath = "/path/to/dir"

        val fileMetadataMap = kafkaFileMetadataManager.javaClass
            .getDeclaredField("fileMetadataMap")
            .apply { isAccessible = true }
            .get(kafkaFileMetadataManager) as ConcurrentHashMap<String, FileMetadataDefault>

        val fileMetadata = FileMetadataDefault().apply {
            this.fileUUID = fileUUID
            this.owner = owner
            this.fileName = fileName
            this.fileSize = totalSize
            this.dirPath = dirPath
        }

        every { chunkMetadata.isFirstChunk() } returns false
        every { chunkMetadata.isLastChunk() } returns true
        every { chunkMetadata.generateKey() } returns "owner-file-uuid"
        fileMetadataMap["owner-file-uuid"] = fileMetadata

        coEvery { metadataRepository.storeMetadata(any()) } just Runs

        // when
        kafkaFileMetadataManager.handleMetadata(topic, chunkMetadata, recordMetadata, chunk)

        // then
        coVerify { metadataRepository.storeMetadata(fileMetadata) }
        assertNull(fileMetadataMap["owner-file-uuid"])
    }


    @Test
    fun `deleteFile should call deleteFileMetadata on repository`() = runTest {
        // given
        val owner = "owner"
        val path = "/path/to/file"
        val fileName = "file.txt"
        val uuid = "file-uuid"
        val limitedFileMetadata = LimitedFileMetadataDefault(
            dirPath = path,
            fileName = fileName,
            fileUUID = uuid,
            owner = owner
        )

        coEvery { metadataRepository.deleteFileMetadata(limitedFileMetadata) } just Runs

        // when
        kafkaFileMetadataManager.deleteFile(owner, path, fileName, uuid)

        // then
        coVerify { metadataRepository.deleteFileMetadata(limitedFileMetadata) }
    }

    @Test
    fun `getFolder should call getFolderMetadata on repository`() = runTest {
        // given
        val owner = "owner"
        val path = "/path/to/folder"
        coEvery { metadataRepository.getFolderMetadata(owner, path) } returns FolderResponse(emptyList(), emptyList())

        // when
        kafkaFileMetadataManager.getFolder(owner, path)

        // then
        coVerify { metadataRepository.getFolderMetadata(owner, path) }
    }

    @Test
    fun `deleteFolder should call deleteFolderMetadata on repository`() = runTest {
        // given
        val owner = "owner"
        val path = "/path/to/folder"
        coEvery { metadataRepository.deleteFolderMetadata(owner, path) } just Runs

        // when
        kafkaFileMetadataManager.deleteFolder(owner, path)

        // then
        coVerify { metadataRepository.deleteFolderMetadata(owner, path) }
    }
}
