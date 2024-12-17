package com.modsen.metadatafilesservice.service

import com.modsen.metadatafilesservice.model.MetadataStatus
import com.modsen.metadatafilesservice.model.ZookeeperFileMetadata
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.test.TestingServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.io.FileNotFoundException


@TestInstance(Lifecycle.PER_CLASS)
class ZookeeperAsyncServiceTest {

    private lateinit var testingServer: TestingServer
    private lateinit var curatorFramework: CuratorFramework
    private lateinit var zookeeperAsyncService: ZookeeperAsyncService

    @BeforeAll
    fun setUp() {
        testingServer = TestingServer()
        testingServer.start()

        curatorFramework = CuratorFrameworkFactory.newClient(
            testingServer.connectString,
            ExponentialBackoffRetry(1000, 3)
        )
        curatorFramework.start()

        zookeeperAsyncService = ZookeeperAsyncService(curatorFramework, "/files")
    }

    @AfterAll
    fun tearDown() {
        curatorFramework.close()
        testingServer.close()
    }

    private fun createFileMetadata(
        fileUUID: String = "123",
        topic: String = "testTopic",
        partition: Int = 1,
        ownerName: String = "testOwner",
        fileName: String = "testFile",
        zooFileName: String = "file_testFile_123",
        fileSize: Long = 1024L,
        totalRecords: Int = 100,
        mimeType: String = "text/plain",
        dirPath: String = "testPath",
    ): ZookeeperFileMetadata {
        return ZookeeperFileMetadata(
            fileUUID = fileUUID,
            topic = topic,
            partition = partition,
            owner = ownerName,
            fileName = fileName,
            zooFileName = zooFileName,
            fileSize = fileSize,
            totalRecords = totalRecords,
            mimeType = mimeType,
            dirPath = dirPath,
            metadataStatus = MetadataStatus.COMPLETED
        )
    }

    @AfterEach
    fun cleanUp() {
        if (curatorFramework.checkExists().forPath("/files") != null)
            curatorFramework.delete().deletingChildrenIfNeeded().forPath("/files")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test saveMetadata`() = runTest {
        val metadata = createFileMetadata()

        val result = zookeeperAsyncService.saveMetadata(metadata)
        Assertions.assertEquals("/files/testOwner/testPath/file_testFile_123", result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test getMetadata`() = runTest {
        val metadata = createFileMetadata()

        zookeeperAsyncService.saveMetadata(metadata)

        val fetchedMetadata = zookeeperAsyncService.getMetadata("testOwner", "/testPath", "testFile", "123")
        Assertions.assertEquals(metadata, fetchedMetadata)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test getMetadata file not found`() = runTest {
        Assertions.assertThrows(FileNotFoundException::class.java) {
            runBlocking {
                zookeeperAsyncService.getMetadata("testOwner", "testPath", "nonExistentFile", "123")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test getFolderMetadata`() = runTest {
        val metadata1 = createFileMetadata(
            fileName = "testFile1",
            zooFileName = "file_testFile1_123",
        )
        val metadata2 = createFileMetadata(
            fileName = "testFile2",
            zooFileName = "file_testFile2_123",
        )

        zookeeperAsyncService.saveMetadata(metadata1)
        zookeeperAsyncService.saveMetadata(metadata2)

        val folderResponse = zookeeperAsyncService.getFolderMetadata("testOwner", "/testPath")
        Assertions.assertEquals(2, folderResponse.files.size)
        Assertions.assertTrue(folderResponse.files.containsAll(listOf(metadata1, metadata2)))
        Assertions.assertEquals(0, folderResponse.folders.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test deleteFileMetadata`() = runTest {
        val metadata = createFileMetadata()

        zookeeperAsyncService.saveMetadata(metadata)

        zookeeperAsyncService.deleteFileMetadata("testOwner", "testPath", "testFile", "123")

        Assertions.assertThrows(FileNotFoundException::class.java) {
            runBlocking {
                zookeeperAsyncService.getMetadata("testOwner", "testPath", "testFile", "123")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test deleteFolderMetadata`() = runTest {
        val metadata1 = createFileMetadata(
            fileName = "testFile1",
            zooFileName = "file_testFile1_123",
        )
        val metadata2 = createFileMetadata(
            fileName = "testFile2",
            zooFileName = "file_testFile2_123",
        )

        zookeeperAsyncService.saveMetadata(metadata1)
        zookeeperAsyncService.saveMetadata(metadata2)

        zookeeperAsyncService.deleteFolderMetadata("testOwner", "/testPath")

        Assertions.assertThrows(FileNotFoundException::class.java) {
            runBlocking {
                zookeeperAsyncService.getMetadata("testOwner", "testPath", "testFile1", "123")
            }
        }

        Assertions.assertThrows(FileNotFoundException::class.java) {
            runBlocking {
                zookeeperAsyncService.getMetadata("testOwner", "testPath", "testFile2", "123")
            }
        }
    }
}