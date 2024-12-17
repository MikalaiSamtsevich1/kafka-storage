package com.modsen.uploaderfilesservice.integration


import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.kafka.ConfluentKafkaContainer
import java.time.Duration


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KafkaFileUploaderControllerTest(@Autowired private val webTestClient: WebTestClient) {

    companion object {
        private lateinit var kafkaContainer: ConfluentKafkaContainer
        private lateinit var mockWebServer: MockWebServer

        @BeforeAll
        @JvmStatic
        fun setUp() {
            kafkaContainer = ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0")
            kafkaContainer.start()

            mockWebServer = MockWebServer()
            mockWebServer.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            kafkaContainer.stop()
            mockWebServer.shutdown()
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("kafka.bootstrap-servers") { kafkaContainer.bootstrapServers }
            registry.add("metadata-service.url") { mockWebServer.url("/").toString() }
        }
    }

    @Test
    fun `uploadChunk should return no content`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val contentRange = "bytes 0-1023/2048"
        val owner = "testOwner"
        val dirPath = "testDir"
        val fileName = "testFile.txt"
        val uuid = "123e4567-e89b-12d3-a456-426614174000"
        val chunk = "Test data chunk".toByteArray()

        webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder.path("/stream/upload")
                    .queryParam("owner", owner)
                    .queryParam("dirPath", dirPath)
                    .queryParam("fileName", fileName)
                    .queryParam("uuid", uuid)
                    .build()
            }
            .header("Content-Range", contentRange)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .bodyValue(chunk)
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `deleteFile should return no content`() {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "files": [
                            {
                                "fileUUID": "123e4567-e89b-12d3-a456-426614174000",
                                "topic": "topic1",
                                "partition": 1,
                                "owner": "testOwner",
                                "fileName": "testFile.txt",
                                "fileSize": 1024,
                                "totalRecords": 100,
                                "mimeType": "text/plain",
                                "dirPath": "testPath",
                                "metadataStatus": "IN_PROGRESS",
                                "createdAt": "2024-12-12T00:00:00",
                                "zooFileName": "zooFile.txt"
                            }
                        ],
                        "folders": ["folder1", "folder2"]
                    }
                """
                )
        )

        val owner = "testOwner"
        val fileName = "testFile.txt"
        val fileUUID = "123e4567-e89b-12d3-a456-426614174000"
        val path = "testPath"

        webTestClient.delete()
            .uri { uriBuilder ->
                uriBuilder.path("/file")
                    .queryParam("owner", owner)
                    .queryParam("fileName", fileName)
                    .queryParam("fileUUID", fileUUID)
                    .queryParam("path", path)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `deleteFolder should return no content`() {
        val webTestClient = WebTestClient.bindToServer()
            .baseUrl(mockWebServer.url("/").toString())
            .responseTimeout(Duration.ofSeconds(10))
            .build()
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "files": [
                            {
                                "fileUUID": "123e4567-e89b-12d3-a456-426614174000",
                                "topic": "topic1",
                                "partition": 1,
                                "owner": "testOwner",
                                "fileName": "testFile.txt",
                                "fileSize": 1024,
                                "totalRecords": 100,
                                "mimeType": "text/plain",
                                "dirPath": "testPath",
                                "metadataStatus": "IN_PROGRESS",
                                "createdAt": "2024-12-12T00:00:00",
                                "zooFileName": "zooFile.txt"
                            }
                        ],
                        "folders": ["folder1", "folder2"]
                    }
                """
                )
        )

        val owner = "testOwner"
        val path = "testPath"

        webTestClient.delete()
            .uri { uriBuilder ->
                uriBuilder.path("/folder")
                    .queryParam("owner", owner)
                    .queryParam("path", path)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
    }
}
