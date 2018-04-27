package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test

class AwsKinesisInboundGatewayTest {

    val worker = mock<Worker> {  }
    val eventHandler = { _: FooCreatedEvent, _: EventMetadata -> }
    val kinesisListener = object : KinesisListener<FooCreatedEvent, EventMetadata> {
        override fun streamName(): String = "foo-event-stream"
        override fun handle(data: FooCreatedEvent, metadata: EventMetadata) {
        }
    }

    val workerFactory: WorkerFactory = mock {
        on {
            worker(any<KinesisListener<FooCreatedEvent, EventMetadata>>())
        } doReturn worker
    }

    val workerStarter: WorkerStarter = mock {  }

    val inboundGateway = AwsKinesisInboundGateway(workerFactory, workerStarter)

    @Test
    fun `when registering a listener instance it should create worker`() {
        inboundGateway.register(kinesisListener)
        verify(workerFactory).worker(kinesisListener)
    }

    @Test
    fun `when registering a listener instance it should run worker`() {
        inboundGateway.register(kinesisListener)
        verify(workerStarter).start(worker)
    }
}