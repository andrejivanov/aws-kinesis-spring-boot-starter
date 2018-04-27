package de.bringmeister.spring.aws.kinesis

import de.bringmeister.connect.erpproductfacade.ports.event.KotlinListenerTest
import org.slf4j.LoggerFactory

class KotlinTestListener : KinesisListener<FooCreatedEvent, EventMetadata> {
    private val log = LoggerFactory.getLogger(this.javaClass.name)
    override fun streamName(): String = "foo-event-stream"
    override fun handle(data: FooCreatedEvent, metadata: EventMetadata) {
        log.info("Kotlin Kinesis listener caught message")
        KotlinListenerTest.latch.countDown()
    }
}