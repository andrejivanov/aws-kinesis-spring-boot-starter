package de.bringmeister.spring.aws.kinesis

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class KinesisClientProviderTest {

    val assumeRoleCredentialsProviderFactory: AWSCredentialsProviderFactory = mock { }
    var clientProvider : KinesisClientProvider = mock {  }

    @Before
    fun setUp() {
        whenever(assumeRoleCredentialsProviderFactory.credentials(any())).thenReturn(mock { })

        val producerSettings = ProducerSettings()
        producerSettings.awsAccountId = "2349724378923"
        producerSettings.iamRoleToAssume = "bar-iam-role"
        producerSettings.streamName = "my-stream"

        val settings = AwsKinesisSettings()
        settings.consumerGroup = "any-consumer-group"
        settings.kinesisUrl = "http://any-example.com"
        settings.producer.add(producerSettings)

        clientProvider = KinesisClientProvider(assumeRoleCredentialsProviderFactory, settings)
    }

    @Test
    fun `should create Kinesis client`() {
        val kinesisClient = clientProvider.clientFor("my-stream")
        assertThat(kinesisClient).isNotNull
    }

    @Test
    fun `should obtain Kinesis credentials`() {
        clientProvider.clientFor("my-stream")
        verify(assumeRoleCredentialsProviderFactory).credentials("arn:aws:iam::2349724378923:role/bar-iam-role")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception for unknown stream`() {
        clientProvider.clientFor("unknown-stream")
    }
}