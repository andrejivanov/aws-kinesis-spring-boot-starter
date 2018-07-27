package de.bringmeister.spring.aws.kinesis

import javax.validation.constraints.NotEmpty

data class FooCreatedEvent(@get: NotEmpty val foo: String)
data class EventMetadata(val sender: String)