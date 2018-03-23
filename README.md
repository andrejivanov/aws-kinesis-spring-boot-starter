# aws-kinesis-spring-boot-starter
[![Build Status](https://img.shields.io/travis/bringmeister/aws-kinesis-spring-boot-starter/master.svg)](https://travis-ci.org/bringmeister/aws-kinesis-spring-boot-starter)
[![Coverage Status](https://img.shields.io/coveralls/bringmeister/aws-kinesis-spring-boot-starter/master.svg)](https://coveralls.io/r/bringmeister/aws-kinesis-spring-boot-starter)

## Dependencies
- Spring 4.3.0 or higher
- [Jackson](https://github.com/FasterXML/jackson)

## Installation
Add the following dependency to your project:
```
repositories {
    ...
    maven { url 'https://jitpack.io' }
}
compile 'com.github.bringmeister:aws-kinesis-spring-boot-starter:v0.0.1'
```

## Configuration
### 1. Define event streams in application.yml

Configure the streams you want to consume and/or publish to in your application.yml
by providing the stream name, the id of the aws account this streams belongs to and 
iam role that allows you to read and/or write to the stream.
```
aws:
  kinesis:
    consumerGroup: example-service
    consumer:
      - stream-name: foo-event-stream
        aws-account-id: "000000000000"
        iam-role-to-assume: ExampleServiceKinesisConsumer
      - stream-name: bar-event-stream
        aws-account-id: "111111111111"
        iam-role-to-assume: ExampleServiceKinesisConsumer
    producer:
      - stream-name: foo-event-stream
        aws-account-id: "000000000000"
        iam-role-to-assume: ExampleServiceKinesisProducer
```

### 2. Define event model
Define the Event model class by implementing `de.bringmeister.spring.aws.kinesis.Event` interface.

```kotlin
data class FooEvent(val metadata: EventMetadata, val data: Foo) : Event {
    companion object {
        const val STREAM_NAME = "foo-event-stream" //maps to stream name in application.yml
    }
    override fun streamName() = STREAM_NAME
}

data class EventMetadata(val occurredAt: OffsetDateTime)
data class Foo(val foo: String)
```

## Usage
aws-kinesis-spring-boot-starter provides two spring-beans for publishing and consuming of event streams:
- `de.bringmeister.spring.aws.kinesis.AwsKinesisInboundGateway`
- `de.bringmeister.spring.aws.kinesis.AwsKinesisOutboundGateway`

### 2. Publish Events
Inject `AwsKinesisOutboundGateway`-bean wherever you like and pass an event to the `send()`-method.
```kotlin
fun send(event: Event)
```

Example:
```kotlin
@Service
class EventProducer(private val gateway: AwsKinesisOutboundGateway) {
    fun sendAnyFooEvent() {        
        gateway.send(FooEvent(metadata = EventMetadata(occurredAt = OffsetDateTime.now()), data = Foo("any foo value")))
    }
}
```

### 3. Consume Events
Inject `AwsKinesisInboundGateway` wherever you like and call `listen()`. You need to provide the name of the stream you defined in the application.yml, 
the Class of the Event you implemented (needed for deserialization of json payload) and the method to process the event.
```kotlin
fun <T : Event> listen(streamName: String, eventClass: Class<T>, process: (T) -> Unit)
```

Example:
```kotlin
@Service
class EventConsumer(private val gateway: AwsKinesisInboundGateway) {
    
    @Scheduled(fixedDelay = (60 * 1000))
    fun listenForFooEvents() {
        gateway.listen(FooEvent.STREAM_NAME, FooEvent::class.java, { event -> println("Processing Event $event") })
    }
}
```