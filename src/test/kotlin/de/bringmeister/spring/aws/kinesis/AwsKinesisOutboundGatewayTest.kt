package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test

class AwsKinesisOutboundGatewayTest {

    val requestFactory = mock<RequestFactory> { }
    val clientProvider = mock<AwsKinesisClientProvider> { }
    val properties = mock<AwsKinesisSettings> {
        val producerSettings = mock<ProducerSettings> {
            on { streamName }.thenReturn("foo-stream")
        }

        on { producer }.thenReturn(mutableListOf(producerSettings))
    }

    val unit = AwsKinesisOutboundGateway(properties, clientProvider, requestFactory)

    @Test
    fun `should create and send kinesis request`() {
        val request = mock<PutRecordRequest> { }
        val producer = mock<AmazonKinesis> { }

        whenever(requestFactory.request(any<KinesisEvent<FooCreatedEvent, EventMetadata>>())).thenReturn(request)
        whenever(clientProvider.producer("foo-stream")).thenReturn(producer)
        whenever(producer.putRecord(any())).thenReturn(mock { })

        unit.initKinesisClients()
        val event = FooCreatedEvent("any-value")
        val metadata = mock<EventMetadata> { }
        unit.send("foo-stream", data = event, metadata = metadata)

        val captor = argumentCaptor<KinesisEvent<FooCreatedEvent, EventMetadata>>()
        verify(requestFactory).request(captor.capture())
        assertThat(captor.firstValue.streamName(), equalTo("foo-stream"))
        assertThat(captor.firstValue.data(), equalTo(event))
        assertThat(captor.firstValue.metadata(), equalTo(metadata))
        verify(clientProvider).producer("foo-stream")
        verify(producer).putRecord(request)
    }
}

class RequestFactoryTest {

    val objectMapper = mock<ObjectMapper> {
        on { writeValueAsBytes(any()) }.thenReturn("any-string".toByteArray())
    }

    val unit = RequestFactory(objectMapper)

    @Test
    fun `should use event stream name for request`() {
        val request = unit.request(KinesisEventWrapper<FooCreatedEvent, EventMetadata>("foo-stream", data = FooCreatedEvent("any-value"), metadata = mock { }))

        assertThat(request.streamName, equalTo("foo-stream"))
    }

    @Test
    fun `should serialize event using object mapper`() {
        val event = KinesisEventWrapper<FooCreatedEvent, EventMetadata>("foo-stream", data = FooCreatedEvent("any-value"), metadata = mock { })

        val request = unit.request(event)

        verify(objectMapper).writeValueAsBytes(event)
        assertThat(request.streamName, equalTo("foo-stream"))
    }
}