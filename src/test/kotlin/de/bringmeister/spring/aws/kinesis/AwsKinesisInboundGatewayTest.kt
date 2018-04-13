package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test


class AwsKinesisInboundGatewayTest : AbstractTest() {

    val clientProvider: AwsKinesisClientProvider = mock {
        on { consumerConfig(any()) } doReturn mock<KinesisClientLibConfiguration> { }
    }

    val workerFactory: WorkerFactory = mock {
        on {
            worker(any(), any<RecordHandler<FooCreatedEvent, EventMetadata>>())
        } doReturn mock<Worker> { }
    }

    val unit = AwsKinesisInboundGateway(clientProvider, workerFactory)

    @Test
    fun `should get client configuration by stream name`() {
        val eventHandler = { _: FooCreatedEvent, _: EventMetadata -> }
        unit.listen("foo-stream", eventHandler, FooCreatedEvent::class.java, EventMetadata::class.java)

        verify(clientProvider).consumerConfig("foo-stream")
    }

    @Test
    fun `should get worker by client configuration`() {
        val eventHandler = { _: FooCreatedEvent, _: EventMetadata -> }
        val clientConfig: KinesisClientLibConfiguration = mock { }
        whenever(clientProvider.consumerConfig("foo-stream")).thenReturn(clientConfig)

        unit.listen("foo-stream", eventHandler, FooCreatedEvent::class.java, EventMetadata::class.java)

        verify(workerFactory).worker(eq(clientConfig), eq(DefaultRecordHandler("foo-stream", FooCreatedEvent::class.java, EventMetadata::class.java, eventHandler)))
    }

    @Test
    fun `should run worker`() {
        val worker: Worker = mock { }
        whenever(workerFactory.worker(any(), any<RecordHandler<*, *>>())).thenReturn(worker)

        unit.listen("foo-stream", { _: FooCreatedEvent, _: EventMetadata -> }, FooCreatedEvent::class.java, EventMetadata::class.java)

        verify(worker).run()
    }
}