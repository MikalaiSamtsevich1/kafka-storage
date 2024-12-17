package com.modsen.uploaderfilesservice.config

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions


@Configuration
class KafkaConfig(
    @Value("\${kafka.bootstrap-servers}") private val bootstrapServers: String,

    @Value("\${kafka.producer.key-serializer}") private val producerKeySerializer: String,
    @Value("\${kafka.producer.value-serializer}") private val producerValueSerializer: String,
    @Value("\${kafka.producer.compression-type}") private val compressionType: String,
    @Value("\${kafka.producer.linger-ms}") private val lingerMs: String,
    @Value("\${kafka.producer.batch-size}") private val batchSize: String
) {

    @Bean
    fun kafkaSender(): KafkaSender<String, ByteArray> {
        val producerProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to producerKeySerializer,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to producerValueSerializer,
            ProducerConfig.COMPRESSION_TYPE_CONFIG to compressionType,
            ProducerConfig.LINGER_MS_CONFIG to lingerMs,
            ProducerConfig.BATCH_SIZE_CONFIG to batchSize,
        )
        val senderOptions = SenderOptions.create<String, ByteArray>(producerProps)
        return KafkaSender.create(senderOptions)
    }

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs = mapOf(
            "bootstrap.servers" to bootstrapServers
        )
        return KafkaAdmin(configs)
    }

    @Bean
    fun adminClient(kafkaAdmin: KafkaAdmin): AdminClient {
        return AdminClient.create(kafkaAdmin.configurationProperties)
    }
}