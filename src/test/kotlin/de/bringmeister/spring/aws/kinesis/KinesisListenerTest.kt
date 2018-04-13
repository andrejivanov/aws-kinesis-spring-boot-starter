package de.bringmeister.spring.aws.kinesis

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

class KinesisListenerTest {

    @Test
    fun `should create default listener`() {

        val listener = KinesisListener.build("my-stream", { data: FooCreatedEvent, metadata: EventMetadata -> println("$data, $metadata") })

        assertThat(listener.streamName(), equalTo("my-stream"))
        assertThat(listener.data(), equalTo(FooCreatedEvent::class.java))
        assertThat(listener.metadata(), equalTo(EventMetadata::class.java))
    }

    @Test
    fun `should create default listener with explicit types`() {

        val listener = KinesisListener.build("my-stream", { data, metadata -> println("$data, $metadata") } , FooCreatedEvent::class.java, EventMetadata::class.java)

        assertThat(listener.streamName(), equalTo("my-stream"))
        assertThat(listener.data(), equalTo(FooCreatedEvent::class.java))
        assertThat(listener.metadata(), equalTo(EventMetadata::class.java))
    }
}