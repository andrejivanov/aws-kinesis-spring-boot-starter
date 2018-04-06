package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = [
    "aws.kinesis.kinesisUrl=http://example.org/kinesis",
    "aws.kinesis.region=us-east-1",
    "aws.kinesis.consumer[0].metricsLevel=DETAILED",
    "aws.kinesis.consumer[0].dynamoDBSettings.url=http://example.org/dynamo",
    "aws.kinesis.consumer[0].dynamoDBSettings.leaseTableReadCapacity=5",
    "aws.kinesis.consumer[0].dynamoDBSettings.leaseTableWriteCapacity=8"
])
class AwsKinesisCustomSettingsTest : AbstractTest() {

    @Autowired
    lateinit var settings: AwsKinesisSettings

    @Test
    fun `should allow override of default kinesis settings`() {
        assertThat(settings.kinesisUrl, equalTo("http://example.org/kinesis"))
        assertThat(settings.region, equalTo("us-east-1"))
    }

    @Test
    fun `should allow override of default consumer settings`() {
        assertThat(settings.consumer[0].dynamoDBSettings.url, equalTo("http://example.org/dynamo"))
        assertThat(settings.consumer[0].metricsLevel, equalTo(MetricsLevel.DETAILED.name))
        assertThat(settings.consumer[0].dynamoDBSettings.leaseTableReadCapacity, equalTo(5))
        assertThat(settings.consumer[0].dynamoDBSettings.leaseTableWriteCapacity, equalTo(8))
    }
}