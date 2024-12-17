package com.modsen.downloaderfilesservice.service


import com.modsen.downloaderfilesservice.model.LimitedFileMetadataDefault
import com.modsen.downloaderfilesservice.model.ZookeeperFileMetadata
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverRecord
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class KafkaStorageAsyncServiceTest {

    private val kafkaReceiverService: KafkaReceiverService = mockk()
    private val zookeeperFileMetadataStorage: ZookeeperFileMetadataStorage = mockk()

    private val kafkaStorageAsyncService = KafkaStorageAsyncService(
        kafkaReceiverService, zookeeperFileMetadataStorage
    )

    @Test
    fun `test receiveChunks processes messages correctly`() = runTest {
        val limitedFileMetadata = LimitedFileMetadataDefault(
            owner = "testOwner",
            dirPath = "/testDir",
            fileName = "testFile",
            fileUUID = "123e4567-e89b-12d3-a456-426614174000"
        )

        val fileMetadata = ZookeeperFileMetadata(
            owner = "testOwner",
            fileUUID = "123e4567-e89b-12d3-a456-426614174000",
            fileName = "testFile",
            totalRecords = 2,
        )

        val kafkaReceiver: KafkaReceiver<String, ByteArray> = mockk()
        val record1 = mockk<ReceiverRecord<String, ByteArray>>()
        val record2 = mockk<ReceiverRecord<String, ByteArray>>()

        coEvery { zookeeperFileMetadataStorage.getMetadata(limitedFileMetadata) } returns fileMetadata

        val mockFlow = flowOf(record1, record2)
        every { kafkaReceiver.receive() } returns mockFlow.asFlux()

        every { record1.key() } returns "testOwner123e4567-e89b-12d3-a456-426614174000testFile"
        every { record1.value() } returns "chunk1".toByteArray()
        every { record1.receiverOffset().acknowledge() } just Runs

        every { record2.key() } returns "testOwner123e4567-e89b-12d3-a456-426614174000testFile"
        every { record2.value() } returns "chunk2".toByteArray()
        every { record2.receiverOffset().acknowledge() } just Runs

        coEvery { kafkaReceiverService.getSpecificKafkaReceiver(fileMetadata) } returns kafkaReceiver

        val result = kafkaStorageAsyncService.receiveChunks(limitedFileMetadata).toList()

        assertEquals(2, result.size)
        assertEquals("chunk1", result[0].toString(Charsets.UTF_8))
        assertEquals("chunk2", result[1].toString(Charsets.UTF_8))

        coVerify(exactly = 1) { zookeeperFileMetadataStorage.getMetadata(limitedFileMetadata) }
        coVerify(exactly = 1) { kafkaReceiverService.getSpecificKafkaReceiver(fileMetadata) }
        verify { record1.receiverOffset().acknowledge() }
        verify { record2.receiverOffset().acknowledge() }
    }
}
