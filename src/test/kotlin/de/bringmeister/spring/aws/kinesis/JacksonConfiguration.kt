package de.bringmeister.spring.aws.kinesis

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfiguration {

    @Bean
    fun jacksonObjectMapper(): ObjectMapper {
        val mapper = ObjectMapper()

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        return mapper
                .registerModule(KotlinModule())
    }
}
