package de.bringmeister.spring.aws.kinesis

data class Record<out D, out M>(val data: D, val metadata: M)
