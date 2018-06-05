package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.model.PutRecordsRequest
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.ByteBuffer
import java.util.UUID

class RequestFactory(private val objectMapper: ObjectMapper) {

    fun request(streamName: String, event: KinesisEvent<*, *>): PutRecordsRequest {
        return request(streamName, listOf(event))
    }

    fun request(streamName: String, event: List<KinesisEvent<*, *>>): PutRecordsRequest {
        return PutRecordsRequest()
            .withStreamName(streamName)
            .withRecords(
                event.map {
                    PutRecordsRequestEntry()
                        .withData(ByteBuffer.wrap(objectMapper.writeValueAsBytes(event)))
                        .withPartitionKey(UUID.randomUUID().toString())
                }
            )
    }
}