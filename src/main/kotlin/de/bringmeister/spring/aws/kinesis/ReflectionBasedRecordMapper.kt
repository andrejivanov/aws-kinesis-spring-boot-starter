package de.bringmeister.spring.aws.kinesis

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.jvmErasure

class ReflectionBasedRecordMapper(private val objectMapper: ObjectMapper): RecordMapper {

    override fun <D, M> deserializeFor(recordData: String, handler: KinesisListener<D, M>): KinesisEvent<D, M> {
        val methods = handler::class::declaredMemberFunctions
        val handleMethod = methods.get().filter({ method -> method.name.equals("handle") })
        val parameters = handleMethod.map({ method -> method.parameters })
        val dataClass = parameters.get(0).get(1).type.jvmErasure.java
        val metadataClass = parameters.get(0).get(2).type.jvmErasure.java
        val type = objectMapper.typeFactory.constructParametricType(KinesisEventWrapper::class.java, dataClass, metadataClass)
        return objectMapper.readValue<KinesisEventWrapper<D, M>>(recordData, type)
    }
}
