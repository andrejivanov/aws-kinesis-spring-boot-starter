package de.bringmeister.spring.aws.kinesis

import com.fasterxml.jackson.databind.ObjectMapper

class ReflectionBasedRecordMapper(private val objectMapper: ObjectMapper): RecordMapper {

    override fun deserializeFor(recordData: String, handler: KinesisListenerProxy): KinesisEvent<*, *> {
        val handleMethod = handler.method
        val parameters = handleMethod.parameters
        val dataClass = parameters.get(0).type
        val metadataClass = parameters.get(1).type
        val type = objectMapper.typeFactory.constructParametricType(KinesisEventWrapper::class.java, dataClass, metadataClass)
        return objectMapper.readValue<KinesisEventWrapper<*, *>>(recordData, type)
    }
}
