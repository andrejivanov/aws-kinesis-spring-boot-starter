package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@EnableConfigurationProperties(AwsKinesisSettings::class)
class TestConfiguration

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestConfiguration::class])
class AwsKinesisDefaultSettingsTest {

    @Autowired
    lateinit var settings: AwsKinesisSettings

    @Test
    fun `should use default region and kinesis url`() {
        assertThat(settings.region, equalTo("eu-central-1"))
        assertThat(settings.kinesisUrl, equalTo("https://kinesis.eu-central-1.amazonaws.com"))
    }
}

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestConfiguration::class])
@ActiveProfiles("producer")
class AwsKinesisProducerSettingsTest {

    @Autowired
    lateinit var settings: AwsKinesisSettings

    @Test
    fun `should read producer settings`() {
        assertThat(settings.producer[0].streamName, equalTo("foo-event-stream"))
        assertThat(settings.producer[0].awsAccountId, equalTo("222222222222"))
        assertThat(settings.producer[0].iamRoleToAssume, equalTo("ExampleKinesisProducerRole"))
    }
}

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestConfiguration::class])
@ActiveProfiles("consumer")
class AwsKinesisConsumerSettingsTest {

    @Autowired
    lateinit var settings: AwsKinesisSettings

    @Test
    fun `should read consumer settings`() {
        assertThat(settings.consumerGroup, equalTo("example-service"))
        assertThat(settings.consumer[0].streamName, equalTo("foo-event-stream"))
        assertThat(settings.consumer[0].awsAccountId, equalTo("111111111111"))
        assertThat(settings.consumer[0].iamRoleToAssume, equalTo("ExampleKinesisConsumerRole"))
    }

    @Test
    fun `should use default consumer settings`() {
        assertThat(settings.consumer[0].dynamoDBSettings.url, equalTo("http://localhost:14568"))
        assertThat(settings.consumer[0].metricsLevel, equalTo(MetricsLevel.NONE.name))
        assertThat(settings.consumer[0].dynamoDBSettings.leaseTableReadCapacity, equalTo(1))
        assertThat(settings.consumer[0].dynamoDBSettings.leaseTableWriteCapacity, equalTo(1))
    }
}

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestConfiguration::class])
@ActiveProfiles("producer", "consumer")
class AwsKinesisConsumerAndProducerSettingsTest {

    @Autowired
    lateinit var settings: AwsKinesisSettings

    @Test
    fun `should read producer and consumer settings`() {
        assertThat(settings.producer[0].streamName, equalTo("foo-event-stream"))
        assertThat(settings.producer[0].awsAccountId, equalTo("222222222222"))
        assertThat(settings.producer[0].iamRoleToAssume, equalTo("ExampleKinesisProducerRole"))

        assertThat(settings.consumerGroup, equalTo("example-service"))
        assertThat(settings.consumer[0].streamName, equalTo("foo-event-stream"))
        assertThat(settings.consumer[0].awsAccountId, equalTo("111111111111"))
        assertThat(settings.consumer[0].iamRoleToAssume, equalTo("ExampleKinesisConsumerRole"))
    }
}

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestConfiguration::class])
@TestPropertySource(properties = [
    "aws.kinesis.kinesisUrl=http://example.org/kinesis",
    "aws.kinesis.region=us-east-1",
    "aws.kinesis.consumer[0].metricsLevel=DETAILED",
    "aws.kinesis.consumer[0].dynamoDBSettings.url=http://example.org/dynamo",
    "aws.kinesis.consumer[0].dynamoDBSettings.leaseTableReadCapacity=5",
    "aws.kinesis.consumer[0].dynamoDBSettings.leaseTableWriteCapacity=8"
])
class AwsKinesisCustomSettingsTest {

    @Autowired
    lateinit var settings: AwsKinesisSettings

    @Test
    fun `should allow override of default region and kinesis url`() {
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