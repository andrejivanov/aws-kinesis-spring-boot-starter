package de.bringmeister.spring.aws.kinesis

data class DefaultRecordHandler<D, M>(val streamName: String,
                                      val data: Class<D>,
                                      val metadata: Class<M>,
                                      val handler: (D, M) -> Unit): RecordHandler<D, M> {

    override fun streamName(): String = streamName
    override fun data(): Class<D> = data
    override fun metadata(): Class<M> = metadata
    override fun handle(data: D, metadata: M) = handler(data, metadata)
}