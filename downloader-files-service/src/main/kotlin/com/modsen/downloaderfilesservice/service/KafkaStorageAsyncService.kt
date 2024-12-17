package com.modsen.downloaderfilesservice.service

import com.modsen.downloaderfilesservice.model.LimitedFileMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.reactive.asFlow
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class KafkaStorageAsyncService(
    private val kafkaReceiverService: KafkaReceiverService,
    private val zookeeperFileMetadataStorage: ZookeeperFileMetadataStorage
) {

    fun receiveChunks(
        limitedFileMetadata: LimitedFileMetadata,
        customProcessor: (Flow<ByteArray>) -> Flow<ByteArray> = { it }
    ): Flow<ByteArray> = flow {
        val currentPosition = AtomicLong(0L)

        val fileMetadata = zookeeperFileMetadataStorage.getMetadata((limitedFileMetadata))

        val kafkaReceiver = kafkaReceiverService.getSpecificKafkaReceiver(fileMetadata)

        val baseFlow = kafkaReceiver.receive().asFlow()
            .filter { record ->
                record.key() == fileMetadata.owner + fileMetadata.fileUUID + fileMetadata.fileName
            }
            .onEach { record ->
                currentPosition.getAndAdd(record.value().size.toLong())
                record.receiverOffset().acknowledge()
            }
            .take(fileMetadata.totalRecords)
            .map { record -> record.value() }

        emitAll(customProcessor(baseFlow))
    }

}
