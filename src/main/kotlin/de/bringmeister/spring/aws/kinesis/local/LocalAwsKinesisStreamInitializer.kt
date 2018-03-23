package de.bringmeister.spring.aws.kinesis.local

import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.kinesis.model.CreateStreamRequest
import com.amazonaws.services.kinesis.model.DescribeStreamRequest
import com.amazonaws.services.kinesis.model.ResourceNotFoundException
import com.amazonaws.services.kinesis.model.StreamDescription
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.TimeUnit

class LocalAwsKinesisStreamInitializer(private val kinesis: AmazonKinesis) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun initStream(streamName: String, shardCount: Int = 1) {
        val describeStreamRequest = DescribeStreamRequest().withStreamName(streamName)
        try {
            val streamDescription = kinesis.describeStream(describeStreamRequest).streamDescription
            log.info("Stream [{}] has a status of [{}].", streamName, streamDescription.streamStatus)

            if (!isActiveStream(streamDescription)) {
                waitForStreamToBecomeAvailable(streamName)
            }
        } catch (ex: ResourceNotFoundException) {
            log.error("Stream [{}] does not exist. Creating it now.", streamName)
            createStreamAndWait(streamName, shardCount)
        }
    }

    private fun isActiveStream(streamDescription: StreamDescription) = "ACTIVE" == streamDescription.streamStatus

    private fun createStreamAndWait(streamName: String, shardCount: Int) {
        val request = CreateStreamRequest()
        request.streamName = streamName
        request.shardCount = shardCount

        kinesis.createStream(request)

        waitForStreamToBecomeAvailable(streamName)
    }

    @Throws(InterruptedException::class)
    private fun waitForStreamToBecomeAvailable(streamName: String) {
        log.info("Waiting for stream [{}] to become ACTIVE...", streamName)

        while (Instant.now().isBefore(timeout())) {
            try {
                val response = kinesis.describeStream(describeStreamRequest(streamName))
                log.info("current stream status: [{}]", response.streamDescription.streamStatus)
                if (isActiveStream(response.streamDescription)) {
                    log.info("stream is active")
                    return
                }
                waitInterval()
            } catch (ex: ResourceNotFoundException) {
                // ResourceNotFound means the stream doesn't exist yet,
                // so ignore this error and just keep polling.
            }
        }

        throw IllegalStateException(String.format("Stream %s never became active", streamName))
    }

    private fun waitInterval() {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))
    }

    private fun timeout() = Instant.now().plusSeconds(30)

    private fun describeStreamRequest(streamName: String): DescribeStreamRequest {
        val request = DescribeStreamRequest()
        request.streamName = streamName
        return request
    }

}
