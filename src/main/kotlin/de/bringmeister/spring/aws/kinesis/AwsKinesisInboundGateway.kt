package de.bringmeister.spring.aws.kinesis

import org.slf4j.LoggerFactory

class AwsKinesisInboundGateway(private val clientProvider: AwsKinesisClientProvider,
                               private val workerFactory: WorkerFactory) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun <DataType, MetadataType> listen(streamName: String, handler: (DataType, MetadataType) -> Unit) {
        log.info("Listening for events on [{}]", streamName)

        val config = clientProvider.consumerConfig(streamName)
        val worker = workerFactory.worker(config, handler)

        log.info("Running consumer to process stream [{}]...", streamName)

        worker.run()
    }
}