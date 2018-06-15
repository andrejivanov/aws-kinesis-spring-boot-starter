package de.bringmeister.spring.aws.kinesis

import org.slf4j.LoggerFactory

class AwsKinesisOutboundGateway(
    private val clientProvider: KinesisClientProvider,
    private val requestFactory: RequestFactory,
    private val streamInitializer: StreamInitializer
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun send(streamName: String, vararg records: Record<*, *>) {
        streamInitializer.createStreamIfMissing(streamName)

        val kinesis = clientProvider.clientFor(streamName)
        val request = requestFactory.request(streamName, *records)

        log.trace("Sending records to stream [{}] \nRecords: [{}]", streamName, records)
        val result = kinesis.putRecords(request)

        log.debug("Successfully send [{}] records", result.records.size)
    }
}
