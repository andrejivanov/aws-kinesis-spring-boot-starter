package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.TimeUnit

class WorkerFactory(private val objectMapper: ObjectMapper) {

    fun <D, M> worker(config: KinesisClientLibConfiguration, handler: (D, M) -> Unit): Worker {

        val processorFactory: () -> (IRecordProcessor) = {
            val configuration = RecordProcessorConfiguration(10, TimeUnit.SECONDS.toMillis(3))
            val eventClass = KinesisEvent::class.java as Class<KinesisEvent<D, M>>
            AwsKinesisRecordProcessor(objectMapper, configuration, handler, eventClass)
        }

        return Worker.Builder()
                .config(config)
                .recordProcessorFactory(processorFactory)
                .build()
    }
}