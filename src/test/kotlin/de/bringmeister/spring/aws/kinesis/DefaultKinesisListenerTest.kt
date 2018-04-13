package de.bringmeister.spring.aws.kinesis

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

class DefaultKinesisListenerTest {

    var count = 0

    val listener = DefaultKinesisListener(
        "my-stream",
        FooCreatedEvent::class.java,
        EventMetadata::class.java,
        { _: FooCreatedEvent, _: EventMetadata -> count++ }
    )

    @Test
    fun `should return the given stream name`() {
        assertThat(listener.streamName(), equalTo("my-stream"))
    }

    @Test
    fun `should return the given data type`() {
        assertThat(listener.data(), equalTo(FooCreatedEvent::class.java))
    }

    @Test
    fun `should return the given meta data type`() {
        assertThat(listener.metadata(), equalTo(EventMetadata::class.java))
    }

    @Test
    fun `should invoke the handler`() {
        listener.handle(FooCreatedEvent("some data"), EventMetadata("test"))
        assertThat(count, equalTo(1))
    }
}
