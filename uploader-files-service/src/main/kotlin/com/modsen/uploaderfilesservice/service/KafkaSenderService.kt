package com.modsen.uploaderfilesservice.service


import com.modsen.uploaderfilesservice.model.ChunkMetadata
import kotlinx.coroutines.reactive.awaitSingle
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord

@Service
class KafkaSenderService(
    private val sender: KafkaSender<String, ByteArray>,
) {

    private val logger: Logger = LoggerFactory.getLogger(KafkaSenderService::class.java)

    suspend fun sendChunk(
        topic: String,
        chunkMetadata: ChunkMetadata,
        chunk: ByteArray
    ): RecordMetadata {
        val record = createRecord(topic, chunkMetadata, chunk)
        return sender.send(Flux.just(record))
            .doOnError { e -> logger.error("Failed to send record: ${e.message}") }
            .awaitSingle()
            .recordMetadata()
    }

    private fun createRecord(
        topic: String,
        chunkMetadata: ChunkMetadata,
        chunk: ByteArray,
    ): SenderRecord<String, ByteArray, String> {
        val kafkaKey = "${chunkMetadata.owner}${chunkMetadata.uuid}${chunkMetadata.fileName}"
        val record = ProducerRecord(topic, 0, kafkaKey, chunk)
        return SenderRecord.create(record, chunkMetadata.position.toString())
    }

}
