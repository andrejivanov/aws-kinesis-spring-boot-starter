package de.bringmeister.spring.aws.kinesis

interface RecordHandler<D, M> {

    companion object {

        fun <D, M, DClass : Class<D>, MClass : Class<M>> build(streamName: String, eventProcessor: (D, M) -> Unit, dataClass: DClass, metadataClass: MClass): RecordHandler<D, M> {
            return DefaultRecordHandler(streamName, dataClass, metadataClass, eventProcessor)
        }

        inline fun <reified D, reified M> build(streamName: String, noinline eventProcessor: (D, M) -> Unit): RecordHandler<D, M> {
            return DefaultRecordHandler(streamName, D::class.java, M::class.java, eventProcessor)
        }
    }

    fun streamName() : String

    fun data(): Class<D>

    fun metadata(): Class<M>

    fun handle(data: D, metadata: M)
}