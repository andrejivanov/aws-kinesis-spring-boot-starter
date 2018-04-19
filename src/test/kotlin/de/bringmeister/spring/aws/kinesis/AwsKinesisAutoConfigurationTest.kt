package de.bringmeister.spring.aws.kinesis

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.mock
import de.bringmeister.spring.aws.kinesis.local.KinesisLocalConfiguration
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [AwsKinesisAutoConfigurationTest.ApplicationConfiguration::class])
class AwsKinesisAutoConfigurationTest {

    @Autowired
    lateinit var inbound: AwsKinesisInboundGateway

    @Autowired
    lateinit var outbound: AwsKinesisOutboundGateway

    @Test
    fun `should inject gateway beans`() {
        checkNotNull(inbound)
        checkNotNull(outbound)
    }

    @Configuration
    @ImportAutoConfiguration(AwsKinesisAutoConfiguration::class, KinesisLocalConfiguration::class)
    class ApplicationConfiguration {

        @Bean
        fun objectMapper() = mock<ObjectMapper> { }
    }
}