package de.bringmeister.spring.aws.kinesis

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RequestFactoryTest {

    val objectMapper = ObjectMapper()
    val requestFactory = RequestFactory(objectMapper)
    val event = KinesisEventWrapper("foo-stream", FooCreatedEvent("any-value"), EventMetadata("test"));

    @Test
    fun `should use event stream name for request`() {
        val request = requestFactory.request(event)
        assertThat(request.streamName).isEqualTo("foo-stream")
    }

    @Test
    fun `should add a random partition key`() {
        val request = requestFactory.request(event)
        assertThat(request.partitionKey).isNotNull()
    }

    @Test
    fun `should serialize message and meta data`() {
        val request = requestFactory.request(event)
        val content = String(request.data.array())
        assertThat(content).isEqualTo("""{"streamName":"foo-stream","data":{"foo":"any-value"},"metadata":{"sender":"test"}}""")
    }
}