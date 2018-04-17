package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.ByteBuffer
import java.util.UUID

class RequestFactory(private val objectMapper: ObjectMapper) {

    fun request(event: KinesisEvent<*, *>): PutRecordRequest =
            PutRecordRequest()
                    .withPartitionKey(UUID.randomUUID().toString())
                    .withStreamName(event.streamName())
                    .withData(ByteBuffer.wrap(objectMapper.writeValueAsBytes(event)))
}