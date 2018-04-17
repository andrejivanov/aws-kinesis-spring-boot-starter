package de.bringmeister.spring.aws.kinesis

interface KinesisEvent<out D, out M> {
    fun streamName(): String
    fun data(): D
    fun metadata(): M
}