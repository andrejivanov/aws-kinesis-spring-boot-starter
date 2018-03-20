package de.bringmeister.spring.aws.kinesis

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test

class AwsKinesisClientProviderTest {

    val properties = mock<AwsKinesisProperties> {
        on { consumerGroup }.thenReturn("any-consumer-group")
        on { metricsLevel }.thenReturn("NONE")
        on { kinesisUrl }.thenReturn("http://any-example.com")
        on { dynamodbUrl }.thenReturn("http://other-example.com")
    }

    val fooStreamProperties = mock<KinesisStream> {
        on { streamName }.thenReturn(FooEvent.STREAM_NAME)
    }

    val credentialsProvider: AWSCredentialsProvider = mock { }
    val assumeRoleCredentialsProviderFactory: AssumeRoleCredentialsProviderFactory = mock { }

    val clientProvider = AwsKinesisClientProvider(
            consumerClientConfigFactory = ConsumerClientConfigFactory(credentialsProvider, properties),
            producerClientFactory = ProducerClientFactory(properties),
            kinesisCredentialsProviderFactory = assumeRoleCredentialsProviderFactory,
            kinesisProperties = properties)

    @Before
    fun setUp() {
        whenever(assumeRoleCredentialsProviderFactory.credentials(any())).thenReturn(mock { })
    }

    @Test
    fun `consumer config should use stream name from kinesis properties`() {
        whenever(properties.consumer).thenReturn(mutableListOf(fooStreamProperties))

        val config = clientProvider.consumerConfig(FooEvent.STREAM_NAME)

        assertThat(config.streamName, equalTo(FooEvent.STREAM_NAME))
    }

    @Test
    fun `consumer config should point to oldest position in stream`() {
        whenever(properties.consumer).thenReturn(mutableListOf(fooStreamProperties))

        val config = clientProvider.consumerConfig(FooEvent.STREAM_NAME)

        assertThat(config.initialPositionInStream, equalTo(InitialPositionInStream.TRIM_HORIZON))
    }

    @Test
    fun `consumer config should use dynamodb and kinesis endpoints defined in kinesis properties`() {
        whenever(properties.kinesisUrl).thenReturn("https://kinesis-endpoint-url.com")
        whenever(properties.dynamodbUrl).thenReturn("https://dynamo-endpoint-url.com")
        whenever(properties.consumer).thenReturn(mutableListOf(fooStreamProperties))

        val config = clientProvider.consumerConfig(FooEvent.STREAM_NAME)

        assertThat(config.kinesisEndpoint, equalTo(properties.kinesisUrl))
        assertThat(config.dynamoDBEndpoint, equalTo(properties.dynamodbUrl))
    }

    @Test
    fun `consumer config should use metrics level defined in kinesis properties`() {
        whenever(properties.metricsLevel).thenReturn(MetricsLevel.DETAILED.name)
        whenever(properties.consumer).thenReturn(mutableListOf(fooStreamProperties))

        val config = clientProvider.consumerConfig(FooEvent.STREAM_NAME)

        assertThat(config.metricsLevel, equalTo(MetricsLevel.fromName(properties.metricsLevel)))
    }

    @Test
    fun `should compose consumer application name from stream name and consumer group`() {
        whenever(properties.consumerGroup).thenReturn("foo-consumer-group")
        whenever(properties.consumer).thenReturn(mutableListOf(fooStreamProperties))

        val config = clientProvider.consumerConfig(FooEvent.STREAM_NAME)

        assertThat(config.applicationName, equalTo("foo-consumer-group_foo-event-stream"))
    }

    @Test
    fun `consumer config should use provided credentials for dynamodb and cloudwatch`() {
        whenever(properties.consumer).thenReturn(mutableListOf(fooStreamProperties))

        val config = clientProvider.consumerConfig(FooEvent.STREAM_NAME)

        assertThat(config.dynamoDBCredentialsProvider, equalTo(credentialsProvider))
        assertThat(config.cloudWatchCredentialsProvider, equalTo(credentialsProvider))
    }

    @Test
    fun `consumer config should use assumeRoleCredentialsProviderFactory to obtain kinesis credentials`() {
        whenever(fooStreamProperties.awsAccountId).thenReturn("123")
        whenever(fooStreamProperties.iamRoleToAssume).thenReturn("iam-role-to-assume")
        whenever(properties.consumer).thenReturn(mutableListOf(fooStreamProperties))

        val kinesisCredentialsProvider = mock<AWSCredentialsProvider> { }
        whenever(assumeRoleCredentialsProviderFactory.credentials("arn:aws:iam::123:role/iam-role-to-assume")).thenReturn(kinesisCredentialsProvider)

        val config = clientProvider.consumerConfig(FooEvent.STREAM_NAME)

        assertThat(config.kinesisCredentialsProvider, equalTo(kinesisCredentialsProvider))
    }

    @Test
    fun `producer should use assumeRoleCredentialsProviderFactory to obtain credentials`() {
        whenever(fooStreamProperties.awsAccountId).thenReturn("123")
        whenever(fooStreamProperties.iamRoleToAssume).thenReturn("iam-role-to-assume")
        whenever(properties.producer).thenReturn(mutableListOf(fooStreamProperties))

        clientProvider.producer(FooEvent.STREAM_NAME)

        verify(assumeRoleCredentialsProviderFactory).credentials("arn:aws:iam::123:role/iam-role-to-assume")
    }

    @Test
    fun `producer should use kinesis endpoint defined in kinesis properties`() {
        whenever(fooStreamProperties.awsAccountId).thenReturn("123")
        whenever(fooStreamProperties.iamRoleToAssume).thenReturn("iam-role-to-assume")
        whenever(properties.producer).thenReturn(mutableListOf(fooStreamProperties))

        clientProvider.producer(FooEvent.STREAM_NAME)

        verify(properties).kinesisUrl
        verify(properties).region
    }

}