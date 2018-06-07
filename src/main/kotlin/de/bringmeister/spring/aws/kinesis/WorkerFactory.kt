package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker

class WorkerFactory(
    private val clientConfigFactory: ClientConfigFactory,
    private val recordMapper: RecordMapper,
    private val settings: AwsKinesisSettings
) {

    fun worker(handler: KinesisListenerProxy): Worker {

        val processorFactory: () -> (IRecordProcessor) = {
            val configuration = RecordProcessorConfiguration(settings.retry.maxRetries, settings.retry.backoffTimeInMilliSeconds)
            AwsKinesisRecordProcessor(recordMapper, configuration, handler)
        }

        val config = clientConfigFactory.consumerConfig(handler.stream)

        return Worker
            .Builder()
            .config(config)
            .recordProcessorFactory(processorFactory)
            .build()
    }
}