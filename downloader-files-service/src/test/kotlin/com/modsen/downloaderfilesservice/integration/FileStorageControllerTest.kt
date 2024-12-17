package com.modsen.downloaderfilesservice.integration

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import java.nio.charset.StandardCharsets
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
    partitions = 1,
    topics = ["test-topic"],
    brokerProperties = ["listeners=PLAINTEXT://localhost:9992", "port=9992"]
)
class FileStorageControllerTest {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, ByteArray>

    companion object {

        private lateinit var mockWebServer: MockWebServer

        @BeforeAll
        @JvmStatic
        fun setUp() {
            mockWebServer = MockWebServer()
            mockWebServer.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            mockWebServer.shutdown()
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("kafka.bootstrap-servers") { "localhost:9992" }
            registry.add("metadata-service.url") { mockWebServer.url("/").toString() }
        }
    }


    @BeforeEach
    fun setUpEach() {
        val configs: MutableMap<String, Any> = HashMap()
        configs[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9992"
        configs[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configs[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java

        val producerFactory = DefaultKafkaProducerFactory<String, ByteArray>(configs)
        kafkaTemplate = KafkaTemplate(producerFactory)
    }

    @Test
    fun `streamFile should return file from Kafka`() {
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
                """
                )
        )
        val testChunk = "Test data chunk".toByteArray(StandardCharsets.UTF_8)
        kafkaTemplate.send("test-topic", "testKey", testChunk).get()

        val owner = "testOwner"
        val path = "testDir"
        val fileName = "testFile.txt"
        val fileUUID = "123e4567-e89b-12d3-a456-426614174000"

        webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/stream/download")
                    .queryParam("owner", owner)
                    .queryParam("path", path)
                    .queryParam("fileName", fileName)
                    .queryParam("fileUUID", fileUUID)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }

}
