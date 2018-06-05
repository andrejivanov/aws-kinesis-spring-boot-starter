package de.bringmeister.spring.aws.kinesis

data class KinesisEventWrapper<out D, out M>(val data: D, val metadata: M) :
    KinesisEvent<D, M> {
    override fun data() = data
    override fun metadata() = metadata
}