package de.bringmeister.spring.aws.kinesis

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

class KinesisListenerTest {

    @Test
    fun `should create default listener`() {

        val listener = object : KinesisListener<FooCreatedEvent, EventMetadata> {
            override fun streamName(): String = "my-stream"
            override fun handle(data: FooCreatedEvent, metadata: EventMetadata) { /* nothing to do */ }
        }

        assertThat(listener.streamName(), equalTo("my-stream"))
    }
}