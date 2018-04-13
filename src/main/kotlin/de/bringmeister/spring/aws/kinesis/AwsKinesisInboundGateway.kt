package de.bringmeister.spring.aws.kinesis

import org.slf4j.LoggerFactory

class AwsKinesisInboundGateway(private val clientProvider: AwsKinesisClientProvider,
                               private val workerFactory: WorkerFactory) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun <D, M, DClass : Class<D>, MClass : Class<M>> listen(streamName: String,
                                                            eventHandler: (D, M) -> Unit,
                                                            dataClass: DClass,
                                                            metadataClass: MClass) {

        listen(RecordHandler.build(streamName, eventHandler, dataClass, metadataClass))
    }

    fun <D, M> listen(handler: RecordHandler<D, M>) {
        log.info("Listening for events on [{}]", handler.streamName())

        val config = clientProvider.consumerConfig(handler.streamName())
        val worker = workerFactory.worker(config, handler)

        log.info("Running consumer to process stream [{}]...", handler.streamName())

        worker.run()
    }
}