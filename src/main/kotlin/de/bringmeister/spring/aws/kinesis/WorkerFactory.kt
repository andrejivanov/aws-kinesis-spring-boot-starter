package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.TimeUnit

class WorkerFactory(private val objectMapper: ObjectMapper) {

    fun <T : Event> worker(config: KinesisClientLibConfiguration, eventClass: Class<T>, process: (T) -> Unit): Worker {

        val processorFactory: () -> (IRecordProcessor) = {
            AwsKinesisRecordProcessor(objectMapper, RecordProcessorConfiguration(10, TimeUnit.SECONDS.toMillis(3)), eventClass, process)
        }

        return Worker.Builder()
                .config(config)
                .recordProcessorFactory(processorFactory)
                .build()
    }
}