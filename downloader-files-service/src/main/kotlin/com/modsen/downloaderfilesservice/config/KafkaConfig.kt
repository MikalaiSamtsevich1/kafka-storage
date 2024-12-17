package com.modsen.downloaderfilesservice.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.ReceiverOptions


@Configuration
class KafkaConfig(
    @Value("\${kafka.bootstrap-servers}") private val bootstrapServers: String,

    @Value("\${kafka.consumer.group-id}") private val groupId: String,
    @Value("\${kafka.consumer.auto-offset-reset}") private val autoOffsetReset: String,
    @Value("\${kafka.consumer.enable-auto-commit}") private val enableAutoCommit: Boolean,
    @Value("\${kafka.consumer.key-deserializer}") private val consumerKeyDeserializer: String,
    @Value("\${kafka.consumer.value-deserializer}") private val consumerValueDeserializer: String,
) {

    @Bean
    fun receiverOptions(): ReceiverOptions<String, ByteArray> {
        val consumerProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to enableAutoCommit.toString(),
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to consumerKeyDeserializer,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to consumerValueDeserializer
        )
        return ReceiverOptions.create<String, ByteArray>(consumerProps)
    }

}