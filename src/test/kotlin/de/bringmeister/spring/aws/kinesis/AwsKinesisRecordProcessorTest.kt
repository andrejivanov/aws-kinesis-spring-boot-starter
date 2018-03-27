package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException
import com.amazonaws.services.kinesis.clientlibrary.exceptions.KinesisClientLibDependencyException
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason
import com.amazonaws.services.kinesis.clientlibrary.types.InitializationInput
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownInput
import com.amazonaws.services.kinesis.model.Record
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import java.nio.ByteBuffer

typealias EventProcessor<T> = (T) -> Unit

class AwsKinesisRecordProcessorTest {

    val objectMapper = mock<ObjectMapper> { }
    val streamCheckpointer = mock<IRecordProcessorCheckpointer> {}
    val configuration = mock<RecordProcessorConfiguration> {
        on { maxRetries } doReturn 1
        on { backoffTimeInMilliSeconds } doReturn 1
    }
    val eventProcessor = mock<EventProcessor<FooEvent>> { }

    val unit = AwsKinesisRecordProcessor(objectMapper, configuration, FooEvent::class.java, eventProcessor)

    @Before
    fun setUp() {
        val initializationInput = mock<InitializationInput> {
            on { shardId }.thenReturn("any-shard")
        }
        unit.initialize(initializationInput)
    }

    @Test
    fun `should deserialize one record`() {
        val payload = """{"data":"{"foo":"any-value"}"}"""

        unit.processRecords(wrap(payload))

        verify(objectMapper).readValue(payload, FooEvent::class.java)
    }

    @Test
    fun `should deserialize all records`() {
        val onePayload = """{"data":"{"foo":"any-value"}"}"""
        val anotherPayload = """{"data":"{"foo":"any-other-value"}"}"""

        unit.processRecords(wrap(onePayload, anotherPayload))

        verify(objectMapper).readValue(onePayload, FooEvent::class.java)
        verify(objectMapper).readValue(anotherPayload, FooEvent::class.java)
    }

    @Test
    fun `should delegate one event to event processor`() {
        val (payload, event) = Pair("""{"data":"{"foo":"any-value"}"}""", FooEvent(data = Foo("any-value")))
        whenever(objectMapper.readValue(payload, FooEvent::class.java)).thenReturn(event)

        unit.processRecords(wrap(payload))

        verify(eventProcessor).invoke(event)
    }

    @Test
    fun `should delegate all events to event processor`() {
        val (onePayload, oneEvent) = Pair("""{"data":"{"foo":"any-value"}"}""", FooEvent(data = Foo("any-value")))
        val (anotherPayload, anotherEvent) = Pair("""{"data":"{"foo":"other-value"}"}""", FooEvent(data = Foo("other-value")))
        whenever(objectMapper.readValue(onePayload, FooEvent::class.java)).thenReturn(oneEvent)
        whenever(objectMapper.readValue(anotherPayload, FooEvent::class.java)).thenReturn(anotherEvent)

        unit.processRecords(wrap(onePayload, anotherPayload))

        verify(eventProcessor).invoke(oneEvent)
        verify(eventProcessor).invoke(anotherEvent)
    }

    @Test
    fun `should retry processing on exception`() {
        val (payload, event) = Pair("""{"data":"{"foo":"any-value"}"}""", FooEvent(data = Foo("any-value")))
        whenever(objectMapper.readValue(payload, FooEvent::class.java)).thenReturn(event)
        whenever(configuration.maxRetries).thenReturn(2)
        whenever(eventProcessor.invoke(event))
                .doThrow(RuntimeException::class)
                .thenReturn(Unit) // stop throwing

        unit.processRecords(wrap(payload))

        verify(eventProcessor, times(2)).invoke(event)
    }

    @Test
    fun `should checkpoint after processing event batch`() {
        val (payload, event) = Pair("""{"data":"{"foo":"any-value"}"}""", FooEvent(data = Foo("any-value")))
        whenever(objectMapper.readValue(payload, FooEvent::class.java)).thenReturn(event)

        unit.processRecords(wrap(payload))

        verify(streamCheckpointer).checkpoint()
    }

    @Test
    fun `should retry checkpointing on dependency exception`() {
        val (payload, event) = Pair("""{"data":"{"foo":"any-value"}"}""", FooEvent(data = Foo("any-value")))
        whenever(objectMapper.readValue(payload, FooEvent::class.java)).thenReturn(event)
        whenever(configuration.maxRetries).thenReturn(2)
        whenever(streamCheckpointer.checkpoint())
                .doThrow(KinesisClientLibDependencyException::class).then { } // stop throwing

        unit.processRecords(wrap(payload))

        verify(streamCheckpointer, times(2)).checkpoint()
    }

    @Test
    fun `should retry checkpointing on throttling exception`() {
        val (payload, event) = Pair("""{"data":"{"foo":"any-value"}"}""", FooEvent(data = Foo("any-value")))
        whenever(objectMapper.readValue(payload, FooEvent::class.java)).thenReturn(event)
        whenever(configuration.maxRetries).thenReturn(2)
        whenever(streamCheckpointer.checkpoint())
                .doThrow(ThrottlingException::class).then { } // stop throwing

        unit.processRecords(wrap(payload))

        verify(streamCheckpointer, times(2)).checkpoint()
    }

    @Test
    fun `shouldn't retry checkpointing when application is shutting down`() {
        val (payload, event) = Pair("""{"data":"{"foo":"any-value"}"}""", FooEvent(data = Foo("any-value")))
        whenever(objectMapper.readValue(payload, FooEvent::class.java)).thenReturn(event)
        whenever(configuration.maxRetries).thenReturn(2)
        whenever(streamCheckpointer.checkpoint())
                .doThrow(ShutdownException::class).then { } // stop throwing

        unit.processRecords(wrap(payload))

        verify(streamCheckpointer).checkpoint()
    }

    @Test
    fun `shouldn't retry checkpointing on invalid state`() {
        val (payload, event) = Pair("""{"data":"{"foo":"any-value"}"}""", FooEvent(data = Foo("any-value")))
        whenever(objectMapper.readValue(payload, FooEvent::class.java)).thenReturn(event)
        whenever(configuration.maxRetries).thenReturn(2)
        whenever(streamCheckpointer.checkpoint())
                .doThrow(InvalidStateException::class).then { } // stop throwing

        unit.processRecords(wrap(payload))

        verify(streamCheckpointer).checkpoint()
    }

    @Test
    fun `should checkpoint on resharding`() {
        val shutdownInput = mock<ShutdownInput> {
            on { shutdownReason } doReturn ShutdownReason.TERMINATE // re-sharding
            on { checkpointer } doReturn streamCheckpointer
        }
        unit.shutdown(shutdownInput)

        verify(streamCheckpointer).checkpoint()
    }

    private fun wrap(vararg eventPayload: String): ProcessRecordsInput {
        return mock {
            val eventRecords = eventPayload.toList().map { payload -> mock<Record> { on { data } doReturn ByteBuffer.wrap(payload.toByteArray()) } }
            on { records }.thenReturn(eventRecords)
            on { checkpointer }.thenReturn(streamCheckpointer)
        }
    }
}