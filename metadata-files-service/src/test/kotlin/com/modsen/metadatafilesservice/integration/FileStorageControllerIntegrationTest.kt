package com.modsen.metadatafilesservice.integration

import com.modsen.metadatafilesservice.controller.FileStorageController
import com.modsen.metadatafilesservice.model.MetadataStatus
import com.modsen.metadatafilesservice.model.ZookeeperFileMetadata
import org.apache.curator.test.TestingServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(FileStorageController::class)
class FileStorageControllerIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    companion object {
        private lateinit var zkTestServer: TestingServer

        @BeforeAll
        @JvmStatic
        fun setUp() {
            zkTestServer = TestingServer()
            zkTestServer.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            zkTestServer.close()
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("zookeeper.url") { zkTestServer.connectString }
        }
    }

    @Test
    fun `test saveFile`() {

        webTestClient.post()
            .uri("/file")
            .bodyValue(createMetadata())
            .exchange()
            .expectStatus().isOk
            .expectBody()

    }

    @Test
    fun `test getFolder`() {
        webTestClient.post()
            .uri("/file")
            .bodyValue(createMetadata())
            .exchange()
            .expectStatus().isOk
            .expectBody()

        webTestClient.get()
            .uri("/folder?owner=testOwner&path=/testDir")
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }

    @Test
    fun `test deleteFile`() {
        webTestClient.post()
            .uri("/file")
            .bodyValue(createMetadata())
            .exchange()
            .expectStatus().isOk
            .expectBody()

        webTestClient.delete()
            .uri("/file?owner=testOwner&fileName=testFile&fileUUID=testUUID&path=/testDir")
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }

    @Test
    fun `test deleteFolder`() {
        webTestClient.post()
            .uri("/file")
            .bodyValue(createMetadata())
            .exchange()
            .expectStatus().isOk
            .expectBody()

        webTestClient.delete()
            .uri("/folder?owner=testOwner&path=/testDir")
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }

    private fun createMetadata() =
        ZookeeperFileMetadata(
            owner = "testOwner",
            dirPath = "/testDir",
            fileName = "testFile",
            fileUUID = "testUUID",
            metadataStatus = MetadataStatus.IN_PROGRESS
        )
}
