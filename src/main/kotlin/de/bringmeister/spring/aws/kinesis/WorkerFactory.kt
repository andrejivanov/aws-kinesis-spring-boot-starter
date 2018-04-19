package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.TimeUnit

class WorkerFactory(private val clientConfigFactory: ClientConfigFactory,
                    private val objectMapper: ObjectMapper) {

    fun <D, M> worker(handler: KinesisListener<D, M>): Worker {

        val processorFactory: () -> (IRecordProcessor) = {
            val configuration = RecordProcessorConfiguration(10, TimeUnit.SECONDS.toMillis(3))
            AwsKinesisRecordProcessor(objectMapper, configuration, handler)
        }

        val config = clientConfigFactory.consumerConfig(handler.streamName())

        return Worker
                .Builder()
                .config(config)
                .recordProcessorFactory(processorFactory)
                .build()
    }
}