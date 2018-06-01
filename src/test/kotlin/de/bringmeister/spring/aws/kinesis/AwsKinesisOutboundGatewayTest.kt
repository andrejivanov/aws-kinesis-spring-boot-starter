package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.kinesis.model.PutRecordRequest
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
    val clientProvider = mock<KinesisClientProvider> { }
    val streamInitializer = mock<StreamInitializer>()
    val outboundGateway = AwsKinesisOutboundGateway(clientProvider, requestFactory, streamInitializer)

    @Test
    fun `should create and send kinesis request`() {
        val request = mock<PutRecordRequest> { }
        val producer = mock<AmazonKinesis> { }

        whenever(requestFactory.request(any<KinesisEvent<FooCreatedEvent, EventMetadata>>())).thenReturn(request)
        whenever(clientProvider.clientFor("foo-stream")).thenReturn(producer)
        whenever(producer.putRecord(any())).thenReturn(mock { })

        val event = FooCreatedEvent("any-value")
        val metadata = mock<EventMetadata> { }
        outboundGateway.send("foo-stream", data = event, metadata = metadata)

        val captor = argumentCaptor<KinesisEvent<FooCreatedEvent, EventMetadata>>()
        verify(requestFactory).request(captor.capture())
        assertThat(captor.firstValue.streamName(), equalTo("foo-stream"))
        assertThat(captor.firstValue.data(), equalTo(event))
        assertThat(captor.firstValue.metadata(), equalTo(metadata))
        verify(clientProvider).clientFor("foo-stream")
        verify(producer).putRecord(request)
    }
}