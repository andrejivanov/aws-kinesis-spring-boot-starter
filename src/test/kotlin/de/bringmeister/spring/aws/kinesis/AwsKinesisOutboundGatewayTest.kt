package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test

class AwsKinesisOutboundGatewayTest {

    val requestFactory = mock<RequestFactory> { }
    val clientProvider = mock<AwsKinesisClientProvider> { }
    val properties = mock<AwsKinesisProperties> {
        val fooStreamProperties = mock<KinesisStream> {
            on { streamName }.thenReturn(FooEvent.STREAM_NAME)
        }

        on { producer }.thenReturn(mutableListOf(fooStreamProperties))
    }

    val unit = AwsKinesisOutboundGateway(properties, clientProvider, requestFactory)

    @Test
    fun `should create and send kinesis request`() {
        val event = FooEvent(data = Foo("any-value"))
        val request = mock<PutRecordRequest> { }
        val producer = mock<AmazonKinesis> {}

        whenever(requestFactory.request(eq(event))).thenReturn(request)
        whenever(clientProvider.producer(FooEvent.STREAM_NAME)).thenReturn(producer)
        whenever(producer.putRecord(any())).thenReturn(mock { })

        unit.initKinesisClients()
        unit.send(event)

        verify(requestFactory).request(event)
        verify(clientProvider).producer(FooEvent.STREAM_NAME)
        verify(producer).putRecord(request)
    }
}

class RequestFactoryTest {

    val objectMapper = mock<ObjectMapper> {
        on { writeValueAsBytes(any()) }.thenReturn("any-string".toByteArray())
    }

    val unit = RequestFactory(objectMapper)

    @Test
    fun `should use events stream name for request`() {
        val request = unit.request(FooEvent(data = Foo("any-value")))

        assertThat(request.streamName, equalTo(FooEvent.STREAM_NAME))
    }

    @Test
    fun `should serialize event data using object mapper`() {
        val event = FooEvent(data = Foo("any-value"))

        val request = unit.request(event)

        verify(objectMapper).writeValueAsBytes(event)
        assertThat(request.streamName, equalTo(FooEvent.STREAM_NAME))
    }
}