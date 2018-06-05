package de.bringmeister.spring.aws.kinesis

import org.slf4j.LoggerFactory

class AwsKinesisOutboundGateway(
    private val clientProvider: KinesisClientProvider,
    private val requestFactory: RequestFactory,
    private val streamInitializer: StreamInitializer
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun <DataType, MetadataType> send(streamName: String, data: DataType, metadata: MetadataType) {
        send(streamName, KinesisEventWrapper(data, metadata))
    }

    fun send(streamName: String, vararg payload: KinesisEvent<*, *>) {

        streamInitializer.createStreamIfMissing(streamName)

        val kinesis = clientProvider.clientFor(streamName)
        val request = requestFactory.request(streamName, *payload)
        val result = kinesis.putRecords(request)

        log.debug("Successfully put records. [stream={}, records={}]", streamName, result.records.size)
    }
}