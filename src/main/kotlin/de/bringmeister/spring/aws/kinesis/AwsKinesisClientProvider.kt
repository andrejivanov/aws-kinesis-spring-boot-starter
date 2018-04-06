package de.bringmeister.spring.aws.kinesis

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream.TRIM_HORIZON
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration
import java.net.InetAddress
import java.util.*

class AwsKinesisClientProvider(private val consumerClientConfigFactory: ConsumerClientConfigFactory,
                               private val producerClientFactory: ProducerClientFactory,
                               private val kinesisCredentialsProviderFactory: AssumeRoleCredentialsProviderFactory,
                               private val kinesisSettings: AwsKinesisSettings) {

    fun producer(streamName: String): AmazonKinesis {
        return producerClientFactory.producer(credentials(producerStreamSettings(streamName)))
    }

    fun consumerConfig(streamName: String): KinesisClientLibConfiguration {
        val consumerSettings = consumerStreamSettings(streamName)
        return consumerClientConfigFactory.consumerConfig(consumerSettings, credentials(consumerSettings))
    }

    private fun credentials(streamSettings: StreamSettings): AWSCredentialsProvider {
        return kinesisCredentialsProviderFactory.credentials(roleToAssume(streamSettings))
    }

    private fun producerStreamSettings(streamName: String) = kinesisSettings.producer.first { it.streamName == streamName }
    private fun consumerStreamSettings(streamName: String) = kinesisSettings.consumer.first { it.streamName == streamName }

    private fun roleToAssume(streamSettings: StreamSettings) = "arn:aws:iam::${streamSettings.awsAccountId}:role/${streamSettings.iamRoleToAssume}"
}

class ConsumerClientConfigFactory(private val credentialsProvider: AWSCredentialsProvider,
                                  private val kinesisSettings: AwsKinesisSettings) {

    fun consumerConfig(consumerSettings: ConsumerSettings, kinesisCredentials: AWSCredentialsProvider): KinesisClientLibConfiguration {
        return clientConfiguration(consumerSettings.streamName, kinesisCredentials)
                .withInitialPositionInStream(TRIM_HORIZON)
                .withKinesisEndpoint(kinesisSettings.kinesisUrl)
                .withDynamoDBEndpoint(consumerSettings.dynamoDBSettings.url)
                .withMetricsLevel(consumerSettings.metricsLevel)
                .withInitialLeaseTableReadCapacity(consumerSettings.dynamoDBSettings.leaseTableReadCapacity)
                .withInitialLeaseTableWriteCapacity(consumerSettings.dynamoDBSettings.leaseTableWriteCapacity)
    }

    private fun clientConfiguration(streamName: String, kinesisCredentials: AWSCredentialsProvider): KinesisClientLibConfiguration {
        return KinesisClientLibConfiguration("${kinesisSettings.consumerGroup}_$streamName", streamName,
                kinesisCredentials, credentialsProvider, credentialsProvider, workerId())
    }

    private fun workerId() = InetAddress.getLocalHost().canonicalHostName + ":" + UUID.randomUUID()
}

class ProducerClientFactory(private val kinesisSettings: AwsKinesisSettings) {

    fun producer(credentials: AWSCredentialsProvider): AmazonKinesis {
        return AmazonKinesisClientBuilder.standard()
                .withCredentials(credentials)
                .withEndpointConfiguration(producerConfiguration())
                .build()
    }

    private fun producerConfiguration() = AwsClientBuilder.EndpointConfiguration(kinesisSettings.kinesisUrl, kinesisSettings.region)
}
