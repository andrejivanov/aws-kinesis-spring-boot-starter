package de.bringmeister.spring.aws.kinesis

data class FooCreatedEvent(val foo: String)
data class EventMetadata(val sender: String)