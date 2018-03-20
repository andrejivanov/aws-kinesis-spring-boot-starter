package de.bringmeister.spring.aws.kinesis

import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class AwsKinesisAutoConfigurationTest : AbstractTest() {

    @Autowired
    lateinit var inbound: AwsKinesisInboundGateway

    @Autowired
    lateinit var outbound: AwsKinesisOutboundGateway

    @Test
    fun `should inject gateway beans`() {
        checkNotNull(inbound)
        checkNotNull(outbound)
    }
}