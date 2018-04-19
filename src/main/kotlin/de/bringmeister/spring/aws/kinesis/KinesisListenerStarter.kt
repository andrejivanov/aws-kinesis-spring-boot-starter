package de.bringmeister.spring.aws.kinesis

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

/**
 * This class will automatically start/register all available Kinesis listeners.
 * As soon as the Spring Boot application has started, this class will iterate
 * through the list of all available listeners and register them.
 *
 * This is a very convenient function for the developer using the library. He must
 * only define the listener - the rest will be managed by the library.
 */
class KinesisListenerStarter(private val inboundGateway: AwsKinesisInboundGateway,
                             private val kinesisListeners: MutableList<KinesisListener<*, *>>) {

    @EventListener(ApplicationReadyEvent::class)
    fun contextRefreshedEvent() = kinesisListeners.forEach({ listener -> inboundGateway.register(listener) })
}
