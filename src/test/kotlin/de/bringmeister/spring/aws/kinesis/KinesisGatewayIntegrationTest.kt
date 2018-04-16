package de.bringmeister.connect.erpproductfacade.ports.event

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import de.bringmeister.spring.aws.kinesis.AwsKinesisAutoConfiguration
import de.bringmeister.spring.aws.kinesis.AwsKinesisInboundGateway
import de.bringmeister.spring.aws.kinesis.AwsKinesisOutboundGateway
import de.bringmeister.spring.aws.kinesis.EventMetadata
import de.bringmeister.spring.aws.kinesis.FooCreatedEvent
import de.bringmeister.spring.aws.kinesis.JacksonConfiguration
import de.bringmeister.spring.aws.kinesis.KinesisListener
import de.bringmeister.spring.aws.kinesis.local.KinesisLocalConfiguration
import org.junit.ClassRule
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.GenericContainer
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

@ActiveProfiles("kinesis-local")
@SpringBootTest(classes = [
    JacksonConfiguration::class,
    JacksonAutoConfiguration::class,
    AwsKinesisAutoConfiguration::class,
    KinesisLocalConfiguration::class
])
@RunWith(SpringRunner::class)
class KinesisGatewayIntegrationTest {

    @Autowired
    lateinit var outbound: AwsKinesisOutboundGateway

    @Autowired
    lateinit var inbound: AwsKinesisInboundGateway

    companion object {
        class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

        @ClassRule
        @JvmField
        val kinesis = KGenericContainer("instructure/kinesalite:latest").withCreateContainerCmdModifier({
            it.withPortBindings(Ports(PortBinding(Ports.Binding("localhost", "14567"), ExposedPort.tcp(4567))))
        })

        @ClassRule
        @JvmField
        val dynamodb = KGenericContainer("richnorth/dynalite:latest").withCreateContainerCmdModifier({
            it.withPortBindings(Ports(PortBinding(Ports.Binding("localhost", "14568"), ExposedPort.tcp(4567))))
        })
    }

    @Test
    @Ignore("Unfortunately bitbucket pipeline runner fails due to permissions needed by testscontainers to create the " +
            "kinesis and dynamodb containers. It might be that the mounted volume is the problem. Bitbucket allows only to bind" +
            "stuff relative to BITBUCKET_CLONE_DIR")
    fun `should send and receive events`() {
        val latch = CountDownLatch(1)
        val fooEvent = FooCreatedEvent("any-field")
        val metadata = EventMetadata("test")

        thread(start = true) {
            inbound.register(object : KinesisListener<FooCreatedEvent, EventMetadata> {
                override fun data(): Class<FooCreatedEvent> = FooCreatedEvent::class.java
                override fun metadata(): Class<EventMetadata> = EventMetadata::class.java
                override fun streamName(): String = "foo-event-stream"
                override fun handle(data: FooCreatedEvent, metadata: EventMetadata) {
                    latch.countDown()
                }
            })
        }

        outbound.send("foo-event-stream", fooEvent, metadata)

        latch.await() // wait for event-listener thread to process event

        // If we come to this point, the latch was counted down!
        // This means the event has been consumed - test succeeded!
    }
}