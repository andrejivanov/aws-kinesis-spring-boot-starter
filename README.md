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

In order to use this library you need to configure some properties in your `application.yml`. 
The following shows the minimal required configuration.
This configuration will allow you to send and receive messages.
Any stream used in your application will be created (as soon as it is first used) if it does not exist.

```
aws:
  kinesis:
    region: eu-cental-1
    kinesis-url: https://kinesis.eu-central-1.amazonaws.com
    consumer-group: example-service
    aws-account-id: "000000000000"
    iam-role-to-assume: ExampleKinesisRole
    create-streams: true
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

In order to consume messages, you need to annotate your listener method with the `KinesisListener` annotation.
Your class must be a Spring Bean annotated withg `@Service` or `@Component`.
It will be picked-up automatically and registered as a listener.
The listener method must take two arguments - one for the actual data and one for the meta data.

Java example:

```Java
@Service
public class MyKinesisListener {

    @KinesisListener(stream = "foo-stream")
    public void handle(MyData data, MyMetadata metadata) {
        System.out.println(data + ", " + metadata);
    }
}
```

See `JavaListenerTest.java` for an example.

Kotlin example:

```Kotlin
@Service
class MyKinesisListener {

    @KinesisListener(stream = "foo-stream")
    override fun handle(data: MyData, metadata: MyMetadata) = println("$data, $metadata")
}
```

See `KotlinListenerTest.kt` for an example.