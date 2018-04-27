package de.bringmeister.spring.aws.kinesis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

class ReflectionBasedRecordMapperTest {

    val messageJson = "{\"streamName\":\"foo-event-stream\",\"data\":{\"foo\":\"any-field\"},\"metadata\":{\"sender\":\"test\"}}"
    val mapper = ObjectMapper().registerModule(KotlinModule())

    @Test
    fun `should deserialize record with a listener implementing the interface`() {

        val handler = object : KinesisListener<FooCreatedEvent, EventMetadata> {
            override fun streamName(): String = "foo-event-stream"
            override fun handle(data: FooCreatedEvent, metadata: EventMetadata) { /* nothing to do */ }
        }

        val recordMapper = ReflectionBasedRecordMapper(mapper)
        val message = recordMapper.deserializeFor(messageJson, handler)

        assertThat(message.streamName(), equalTo("foo-event-stream"))
        assertThat(message.data(), equalTo(FooCreatedEvent("any-field")))
        assertThat(message.metadata(), equalTo(EventMetadata("test")))
    }
}