package de.bringmeister.spring.aws.kinesis

import org.slf4j.LoggerFactory

class AwsKinesisInboundGateway(private val clientProvider: AwsKinesisClientProvider,
                               private val workerFactory: WorkerFactory,
                               private val workerStarter: WorkerStarter) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun <D, M, DClass : Class<D>, MClass : Class<M>> register(streamName: String,
                                                              eventHandler: (D, M) -> Unit,
                                                              dataClass: DClass,
                                                              metadataClass: MClass) {

        register(KinesisListener.build(streamName, eventHandler, dataClass, metadataClass))
    }

    fun <D, M> register(handler: KinesisListener<D, M>) {
        val config = clientProvider.consumerConfig(handler.streamName())
        val worker = workerFactory.worker(config, handler)
        workerStarter.start(worker)
        log.info("Started AWS Kinesis listener. [stream={}, expecting={}]...", handler.streamName(), handler.data().simpleName)
    }
}