package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput
import com.amazonaws.services.kinesis.model.Record
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import java.nio.ByteBuffer

typealias EventProcessor<T> = (T) -> Unit

class AwsKinesisRecordProcessorTest {

    val objectMapper = mock<ObjectMapper> { }
    val configuration = mock<RecordProcessorConfiguration> {
        on { maxRetries }.thenReturn(1)
    }
    val eventProcessor = mock<EventProcessor<FooEvent>> { }

    val unit = AwsKinesisRecordProcessor(objectMapper, configuration, FooEvent::class.java, eventProcessor)

    @Test
    fun `should deserialize record to event`() {
        val eventPayload = """{"data":"{"foo":"any-value"}"}"""
        val processRecordsInput = processRecordsInput(eventPayload)

        unit.processRecords(processRecordsInput)

        verify(objectMapper).readValue(eventPayload, FooEvent::class.java)
    }

    @Test
    fun `should delegate event to event processor`() {
        val eventPayload = """{"data":"{"foo":"any-value"}"}"""
        val processRecordsInput = processRecordsInput(eventPayload)
        val event = FooEvent(data = Foo("any-value"))
        whenever(objectMapper.readValue(eventPayload, FooEvent::class.java)).thenReturn(event)

        unit.processRecords(processRecordsInput)

        verify(eventProcessor).invoke(event)
    }

    private fun processRecordsInput(eventPayload: String): ProcessRecordsInput {
        return mock {
            val record = mock<Record> {
                on { data }.thenReturn(ByteBuffer.wrap(eventPayload.toByteArray()))
            }
            on { records }.thenReturn(mutableListOf(record))

            val checkpointerMock = mock<IRecordProcessorCheckpointer> { }
            on { checkpointer }.thenReturn(checkpointerMock)
        }
    }
}