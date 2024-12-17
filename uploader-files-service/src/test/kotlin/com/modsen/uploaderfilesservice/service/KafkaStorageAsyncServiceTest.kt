package com.modsen.uploaderfilesservice.service


import com.modsen.uploaderfilesservice.model.ChunkMetadata
import com.modsen.uploaderfilesservice.model.FolderResponse
import com.modsen.uploaderfilesservice.model.ZookeeperFileMetadata
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.DeleteTopicsResult
import org.apache.kafka.clients.producer.RecordMetadata
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KafkaStorageAsyncServiceTest {

    private val kafkaSenderService = mockk<KafkaSenderService>(relaxed = true)
    private val kafkaFileMetadataService = mockk<KafkaFileMetadataManager>(relaxed = true)
    private val adminClient = mockk<AdminClient>(relaxed = true)
    private lateinit var kafkaStorageAsyncService: KafkaStorageAsyncService

    @BeforeEach
    fun setUp() {
        kafkaStorageAsyncService = KafkaStorageAsyncService(kafkaSenderService, kafkaFileMetadataService, adminClient)
    }

    @Test
    fun `uploadChunks should send chunk and handle metadata`() = runTest {
        // given
        val topic = "test-topic"
        val chunk = ByteArray(1024)
        val chunkMetadata = mockk<ChunkMetadata>()
        val recordMetadata = mockk<RecordMetadata>()

        coEvery { kafkaSenderService.sendChunk(topic, chunkMetadata, chunk) } returns recordMetadata
        coEvery { kafkaFileMetadataService.handleMetadata(topic, chunkMetadata, recordMetadata, chunk) } just Runs

        // when
        kafkaStorageAsyncService.uploadChunks(topic, chunk, chunkMetadata)

        // then
        coVerify(exactly = 1) { kafkaSenderService.sendChunk(topic, chunkMetadata, chunk) }
        coVerify(exactly = 1) { kafkaFileMetadataService.handleMetadata(topic, chunkMetadata, recordMetadata, chunk) }
    }

    @Test
    fun `deleteFile should delete topic and file metadata`() = runTest {
        // given
        val owner = "owner"
        val path = "/path/to/file"
        val fileName = "file.txt"
        val uuid = "file-uuid"
        val topic = "$owner-$fileName-$uuid"
        val deleteTopicsResult = mockk<DeleteTopicsResult>()

        every { adminClient.deleteTopics(listOf(topic)) } returns deleteTopicsResult
        coEvery { kafkaFileMetadataService.deleteFile(owner, path, fileName, uuid) } just Runs

        // when
        val result = kafkaStorageAsyncService.deleteFile(owner, path, fileName, uuid)

        // then
        assertEquals(deleteTopicsResult, result)
        coVerify(exactly = 1) { kafkaFileMetadataService.deleteFile(owner, path, fileName, uuid) }
    }

    @Test
    fun `deleteFolder should delete topics for all files in folder and remove folder metadata`() = runTest {
        // given
        val owner = "owner"
        val path = "path/to/folder"
        val requestPath = "/path/to/folder"
        val file1 = ZookeeperFileMetadata(owner = owner, fileName = "file1.txt", fileUUID = "uuid1", dirPath = path)
        val file2 = ZookeeperFileMetadata(owner = owner, fileName = "file2.txt", fileUUID = "uuid2", dirPath = path)
        val folder = FolderResponse(files = listOf(file1, file2), folders = listOf())

        coEvery { kafkaFileMetadataService.getFolder(owner, any()) } returns folder
        every {
            adminClient.deleteTopics(listOf("${owner}-${file1.fileName}-${file1.fileUUID}")).all().get()
        } just Awaits
        every {
            adminClient.deleteTopics(listOf("${owner}-${file2.fileName}-${file2.fileUUID}")).all().get()
        } just Awaits
        coEvery { kafkaFileMetadataService.deleteFile(owner, any(), file1.fileName, file1.fileUUID) } just Runs
        coEvery { kafkaFileMetadataService.deleteFile(owner, any(), file2.fileName, file2.fileUUID) } just Runs
        coEvery { kafkaFileMetadataService.deleteFolder(owner, any()) } just Runs

        // when
        kafkaStorageAsyncService.deleteFolder(owner, requestPath)

        // then
        coVerify(exactly = 1) { kafkaFileMetadataService.deleteFile(owner, any(), file1.fileName, file1.fileUUID) }
        coVerify(exactly = 1) { kafkaFileMetadataService.deleteFile(owner, any(), file2.fileName, file2.fileUUID) }
        coVerify(exactly = 1) { kafkaFileMetadataService.deleteFolder(owner, any()) }
    }
}
