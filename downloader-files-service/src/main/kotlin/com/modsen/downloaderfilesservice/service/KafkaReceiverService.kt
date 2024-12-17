package com.modsen.downloaderfilesservice.service


import com.modsen.downloaderfilesservice.model.FileMetadata
import org.apache.kafka.common.TopicPartition
import org.springframework.stereotype.Service
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions

@Service
class KafkaReceiverService(private val receiverOptions: ReceiverOptions<String, ByteArray>) {

    fun getSpecificKafkaReceiver(fileMetadata: FileMetadata): KafkaReceiver<String, ByteArray> {
        val topicPartition = TopicPartition(fileMetadata.topic, fileMetadata.partition)
        val customOptions = receiverOptions
            .assignment(listOf(topicPartition))
        return KafkaReceiver.create(customOptions)
    }
}
