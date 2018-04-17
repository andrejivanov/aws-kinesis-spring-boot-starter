package de.bringmeister.spring.aws.kinesis

data class RecordProcessorConfiguration(val maxRetries: Int, val backoffTimeInMilliSeconds: Long)