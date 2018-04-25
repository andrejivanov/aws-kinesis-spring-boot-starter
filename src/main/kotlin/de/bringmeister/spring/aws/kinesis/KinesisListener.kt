package de.bringmeister.spring.aws.kinesis

/**
 * Generic interface of a Kinesis listener. A Kinesis listener provides a stream name
 * as well as a method which receives the data (and meta data) in order to handle it
 * (e.g. to pass it on to a service).
 *
 * Usage:
 *
 *          @Service
 *          class MyKinesisListener: KinesisListener<MyData, MyMetadata> {
 *              override fun streamName(): String = "my-kinesis-stream"
 *              override fun handle(data: MyData, metadata: MyMetadata) = println("$data, $metadata")
 *          }
 */
interface KinesisListener<D, M> {
    fun streamName() : String
    fun handle(data: D, metadata: M)
}