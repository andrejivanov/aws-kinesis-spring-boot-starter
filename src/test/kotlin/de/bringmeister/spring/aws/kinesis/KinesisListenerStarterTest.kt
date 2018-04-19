package de.bringmeister.spring.aws.kinesis

import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest(classes = [
    TestListener::class,
    KinesisListenerStarter::class
])
@RunWith(SpringRunner::class)
class KinesisListenerStarterTest {

    @MockBean
    lateinit var inboundGateway: AwsKinesisInboundGateway

    @Autowired
    lateinit var testListener: TestListener

    @Test
    fun `should register listener automatically`() {
        verify(inboundGateway).register(testListener)
    }
}

class TestListener : KinesisListener<FooCreatedEvent, EventMetadata> {
    override fun data(): Class<FooCreatedEvent> = FooCreatedEvent::class.java
    override fun metadata(): Class<EventMetadata> = EventMetadata::class.java
    override fun streamName(): String = "foo-event-stream"
    override fun handle(data: FooCreatedEvent, metadata: EventMetadata) {
        // empty
    }
}