package de.bringmeister.spring.aws.kinesis

import org.slf4j.LoggerFactory

class AwsKinesisOutboundGateway(private val clientProvider: KinesisClientProvider,
                                private val requestFactory: RequestFactory) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun <DataType, MetadataType> send(streamName: String, data: DataType, metadata: MetadataType) {
        send(KinesisEventWrapper(streamName, data, metadata))
    }

    private fun send(event: KinesisEvent<*, *>) {

        val streamName = event.streamName()

        log.info("Sending event [{}] to stream [{}]", event, streamName)

        val kinesis = clientProvider.clientFor(streamName)
        val request = requestFactory.request(event)
        val result = kinesis.putRecord(request)

        log.info("Successfully put record. [partitionKey={}, shardId={}, sequenceNumber={}]",
                request.partitionKey, result.shardId, result.sequenceNumber)
    }
}