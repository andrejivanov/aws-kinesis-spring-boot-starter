package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class AwsKinesisDefaultSettingsTest : AbstractTest() {

    @Autowired
    lateinit var settings: AwsKinesisSettings

    @Test
    fun `should use default kinesis settings`() {
        assertThat(settings.kinesisUrl, equalTo("https://kinesis.eu-central-1.amazonaws.com"))
        assertThat(settings.region, equalTo("eu-central-1"))
    }

    @Test
    fun `should use default consumer settings`() {
        assertThat(settings.consumer[0].dynamoDBSettings.url, equalTo("https://dynamodb.eu-central-1.amazonaws.com"))
        assertThat(settings.consumer[0].metricsLevel, equalTo(MetricsLevel.NONE.name))
        assertThat(settings.consumer[0].dynamoDBSettings.leaseTableReadCapacity, equalTo(1))
        assertThat(settings.consumer[0].dynamoDBSettings.leaseTableWriteCapacity, equalTo(1))
    }
}