package de.bringmeister.spring.aws.kinesis.local

import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.kinesis.model.DescribeStreamResult
import com.amazonaws.services.kinesis.model.ResourceNotFoundException
import org.slf4j.LoggerFactory
import java.time.Instant.now

class KinesisStreamInitializer(private val kinesis: AmazonKinesis) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun createStreamIfMissing(streamName: String, shardCount: Int = 1) {
        try {
            val response = kinesis.describeStream(streamName)
            if (!streamIsActive(response)) {
                waitForStreamToBecomeActive(streamName)
            }
        } catch (ex: ResourceNotFoundException) {
            log.info("Creating stream. [streamName=$streamName]")
            kinesis.createStream(streamName, shardCount)
            waitForStreamToBecomeActive(streamName)
        }
        log.info("Stream is active. [streamName=$streamName]")
    }

    private fun waitForStreamToBecomeActive(streamName: String) {
        log.info("Waiting for stream to become active. [streamName=$streamName]")
        val thirtySecondsInTheFuture = now().plusSeconds(30)
        while (now().isBefore(thirtySecondsInTheFuture)) {
            try {
                val response = kinesis.describeStream(streamName)
                log.debug("Current stream status: [${response.streamDescription.streamStatus}]")
                if (streamIsActive(response)) {
                    return
                }
                waitOneSecond()
            } catch (ex: ResourceNotFoundException) {
                // ResourceNotFound means the stream doesn't exist yet,
                // so ignore this error and just keep polling.
            }
        }
        throw IllegalStateException("Stream never became active: $streamName")
    }

    private fun streamIsActive(streamDescription: DescribeStreamResult): Boolean {
        return "ACTIVE" == streamDescription.streamDescription.streamStatus
    }

    private fun waitOneSecond() {
        Thread.sleep(1000)
    }
}
