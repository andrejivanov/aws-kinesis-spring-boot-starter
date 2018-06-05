package de.bringmeister.spring.aws.kinesis

import org.slf4j.LoggerFactory

class AwsKinesisOutboundGateway(
    private val clientProvider: KinesisClientProvider,
    private val requestFactory: RequestFactory,
    private val streamInitializer: StreamInitializer
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun <DataType, MetadataType> send(streamName: String, data: DataType, metadata: MetadataType) {
        sendAll(streamName, listOf(Pair(data, metadata)))
    }

    fun <DataType, MetadataType> sendAll(streamName: String, pairs: List<Pair<DataType, MetadataType>>) {
        val kinesisEvents = pairs.map { KinesisEventWrapper(streamName, it.first, it.second) }
        send(streamName, kinesisEvents)
    }

    private fun send(streamName: String, events: List<KinesisEvent<*, *>>) {

        streamInitializer.createStreamIfMissing(streamName)

        val kinesis = clientProvider.clientFor(streamName)
        val request = requestFactory.request(streamName, events)
        val result = kinesis.putRecords(request)

        log.debug("Successfully put records. [stream={}, records={}]", streamName, result.records.size)
    }
}