package de.bringmeister.spring.aws.kinesis

/**
 * Generic interface of a Kinesis listener. A Kinesis listener provides a stream name
 * as well as the type of the data (and meta data) it consumes. It also provides a handle
 * method which receives the data (and meta data) in order to handle it (e.g. to pass it
 * on to a service).
 *
 * Usage:
 *
 *      val kinesisListener = KinesisListener.build("foo-stream", { data: MyData, metadata: MyMetadata -> println("$data, $metadata") })
 *      awsKinesisInboundGateway.register(kinesisListener)
 *
 * You can also extend this interface by your own class to register an instance of this class.
 */
interface KinesisListener<D, M> {

    companion object {

        fun <D, M, DClass : Class<D>, MClass : Class<M>> build(streamName: String, eventProcessor: (D, M) -> Unit, dataClass: DClass, metadataClass: MClass): KinesisListener<D, M> {
            return DefaultKinesisListener(streamName, dataClass, metadataClass, eventProcessor)
        }

        inline fun <reified D, reified M> build(streamName: String, noinline eventProcessor: (D, M) -> Unit): KinesisListener<D, M> {
            return DefaultKinesisListener(streamName, D::class.java, M::class.java, eventProcessor)
        }
    }

    fun streamName() : String
    fun data(): Class<D>
    fun metadata(): Class<M>
    fun handle(data: D, metadata: M)
}