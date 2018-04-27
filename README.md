aws-kinesis-spring-boot-starter
===============================

[![Build Status](https://img.shields.io/travis/bringmeister/aws-kinesis-spring-boot-starter/master.svg)](https://travis-ci.org/bringmeister/aws-kinesis-spring-boot-starter)
[![Coverage Status](https://img.shields.io/coveralls/bringmeister/aws-kinesis-spring-boot-starter/master.svg)](https://coveralls.io/r/bringmeister/aws-kinesis-spring-boot-starter)
[![Release](https://img.shields.io/github/release/bringmeister/aws-kinesis-spring-boot-starter.svg)](https://github.com/bringmeister/aws-kinesis-spring-boot-starter/releases)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/bringmeister/aws-kinesis-spring-boot-starter/master/LICENSE)

## Dependencies

- Spring 4.3.0 or higher
- [Jackson](https://github.com/FasterXML/jackson)

## Notes

This library is written in Kotlin. 
However, it's perfectly **compatible with Java**. 
Below you will find examples for both languages.

## Installation

Add the following dependency to your project:
```
repositories {
    ...
    maven { url 'https://jitpack.io' }
}
compile "com.github.bringmeister:aws-kinesis-spring-boot-starter:$version"
```

**Note:** See above for the latest version available!

## Configuration

Configure the streams you want to consume and/or publish to in your `application.yml`.
Provide the stream name, the ID of the AWS account this streams belongs to and
IAM role that allows you to read and/or write to the stream.
```
aws:
  kinesis:
    consumerGroup: example-service # required only for consuming
    kinesisUrl: "https://kinesis.eu-central-1.amazonaws.com" #optional
    region: eu-cental-1 #optional
    consumer:
      - streamName: foo-event-stream
        awsAccountId: "000000000000"
        iamRoleToAssume: ExampleServiceKinesisConsumer
        metricsLevel: DETAILED #optional
        dynamoDBSettings:
            url: "https://dynamodb.eu-central-1.amazonaws.com" #optional
            leaseTableReadCapacity: 5 #optional
            leaseTableWriteCapacity: 8 #optional
      - streamName: bar-event-stream
        awsAccountId: "111111111111"
        iamRoleToAssume: ExampleServiceKinesisConsumer
    producer:
      - streamName: foo-event-stream
        awsAccountId: "000000000000"
        iamRoleToAssume: ExampleServiceKinesisProducer
```

## Usage

### Publishing messages

Inject the `AwsKinesisOutboundGateway` wherever you like and pass stream name, data (the actual payload) and metadata to the `send()`-method.

Java example:

```Java
@Service
public class MyService {

    private final AwsKinesisOutboundGateway gateway;

    public MyService(AwsKinesisOutboundGateway gateway) {
        this.gateway = gateway;
    }

    public void sendMyMessage() {
        gateway.send("my-stream", new MyMessage("my content"), new MyMetadata("my metadata")); 
    }
}
```

See `JavaListenerTest.java` for an example.

Kotlin example:

```Kotlin
@Service
class MyService(private val gateway: AwsKinesisOutboundGateway) {
    fun sendMyMessage() {        
        gateway.send("my-stream", MyMessage("my content"), MyMetadata("my metadata"))
    }
}
```

See `KotlinListenerTest.kt` for an example.

The event will be marshalled as JSON using Jackson and send to the Kinesis stream using the credentials defined in the `application.yml`.

````
{
    "data":"my content",
    "metadata":"my metadata"
}
````

### Consuming messages

In order to consume messages, you need to extend the `KinesisListener` interface.
Your class must be a Spring Bean annotated withg `@Service` or `@Component`.
It will be picked-up automatically and registered as a listener.

Java example:

```Java
@Service
public class MyKinesisListener implements KinesisListener<MyData, MyMetadata> {

    public String streamName() {
        return "foo-stream";
    }

    public void handle(MyData data, MyMetadata metadata) {
        System.out.println(data + ", " + metadata);
    }
}
```

See `JavaListenerTest.java` for an example.

Kotlin example:

```Kotlin
@Service
class MyKinesisListener: KinesisListener<MyData, MyMetadata> {

    override fun streamName(): String = "foo-stream"
    override fun handle(data: MyData, metadata: MyMetadata) = println("$data, $metadata")
}
```

See `KotlinListenerTest.kt` for an example.