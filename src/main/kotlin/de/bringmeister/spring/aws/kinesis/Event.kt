package de.bringmeister.spring.aws.kinesis

interface Event {
    fun streamName(): String
}

interface EventProcessor<in T : Event> {
    fun process(event: T)
}