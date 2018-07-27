package de.bringmeister.spring.aws.kinesis

import org.slf4j.LoggerFactory
import javax.validation.ValidationException
import javax.validation.Validator

class AwsKinesisOutboundGateway(
    private val clientProvider: KinesisClientProvider,
    private val requestFactory: RequestFactory,
    private val streamInitializer: StreamInitializer,
    private val validator: Validator? = null
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun send(streamName: String, vararg records: Record<*, *>) {
        streamInitializer.createStreamIfMissing(streamName)

        val violations = validator?.validate(records) ?: setOf()
        if (violations.isNotEmpty()) {
            throw ValidationException("invalid record: $violations")
        }

        val request = requestFactory.request(streamName, *records)
        val kinesis = clientProvider.clientFor(streamName)

        log.trace("Sending records to stream [{}] \nRecords: [{}]", streamName, records)
        val result = kinesis.putRecords(request)

        log.debug("Successfully send [{}] records", result.records.size)
    }
}
