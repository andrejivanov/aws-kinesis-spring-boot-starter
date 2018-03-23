package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException
import com.amazonaws.services.kinesis.clientlibrary.exceptions.KinesisClientLibDependencyException
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason
import com.amazonaws.services.kinesis.clientlibrary.types.InitializationInput
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownInput
import com.amazonaws.services.kinesis.model.Record
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.nio.charset.Charset

class AwsKinesisRecordProcessor<T : Event>(private val objectMapper: ObjectMapper,
                                           private val configuration: RecordProcessorConfiguration,
                                           private val eventClass: Class<T>,
                                           private val process: (T) -> (Unit)) : IRecordProcessor {

    private val log = LoggerFactory.getLogger(this.javaClass.name)

    override fun initialize(initializationInput: InitializationInput?) {
        log.info("Initializing worker for shard ${initializationInput!!.shardId}")
    }

    override fun processRecords(processRecordsInput: ProcessRecordsInput?) {
        processRecordsWithRetries(processRecordsInput!!.records)
        checkpoint(processRecordsInput.checkpointer)
    }

    private fun processRecordsWithRetries(records: List<Record>) {
        for (record in records) {
            var processedSuccessfully = false
            for (i in 0 until configuration.maxRetries) {
                try {
                    processRecord(record)

                    processedSuccessfully = true
                    break
                } catch (t: Throwable) {
                    log.warn("Caught throwable while processing record [{}]", record, t)
                }

                backoff()
            }

            if (!processedSuccessfully) {
                log.error("Couldn't process record $record. Skipping the record.")
            }
        }
    }

    private fun processRecord(record: Record) {
        val data = Charset.forName("UTF-8").decode(record.data).toString()
        log.info("Received Event [{}]", data)

        val event = objectMapper.readValue(data, eventClass)

        process(event)
    }

    private fun checkpoint(checkpointer: IRecordProcessorCheckpointer) {
        log.debug("Checkpointing")
        val maxRetries = configuration.maxRetries
        for (retries in 0 until maxRetries) {
            try {
                checkpointer.checkpoint()
                break
            } catch (se: ShutdownException) {
                log.info("Application is shutting down. Skipping checkpoint.", se)
                break
            } catch (e: ThrottlingException) {
                if (retries > maxRetries) {
                    log.error("Checkpoint failed after ${retries + 1} attempts.", e)
                    break
                }
                log.info("Transient issue when checkpointing - attempt ${retries + 1} of $maxRetries", e)
            } catch (e: InvalidStateException) {
                log.error("Cannot save checkpoint. Please check corresponding DynamoDB table.", e)
                break
            } catch (e: KinesisClientLibDependencyException) {
                log.error("Can't store checkpoint. Backoff and retry.")
            }

            backoff()
        }
    }

    private fun backoff() {
        try {
            Thread.sleep(configuration.backoffTimeInMilliSeconds)
        } catch (e: InterruptedException) {
            log.debug("Interrupted sleep", e)
        }
    }

    override fun shutdown(shutdownInput: ShutdownInput?) {
        log.info("Shutting down record processor")
        // Important to checkpoint after reaching end of shard, so we can start processing data from child shards.
        if (shutdownInput?.shutdownReason == ShutdownReason.TERMINATE) {
            checkpoint(shutdownInput.checkpointer)
        }
    }
}

data class RecordProcessorConfiguration(val maxRetries: Int, val backoffTimeInMilliSeconds: Long)
