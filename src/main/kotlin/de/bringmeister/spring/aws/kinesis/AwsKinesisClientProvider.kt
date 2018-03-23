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
                               private val kinesisProperties: AwsKinesisProperties) {

    fun producer(streamName: String) = producerClientFactory.producer(credentials(producerStreamSettings(streamName)))
    fun consumerConfig(streamName: String) = consumerClientConfigFactory.consumerConfig(streamName, credentials(consumerStreamSettings(streamName)))

    private fun credentials(streamSettings: KinesisStream): AWSCredentialsProvider {
        return kinesisCredentialsProviderFactory.credentials(roleToAssume(streamSettings))
    }

    private fun producerStreamSettings(streamName: String) = kinesisProperties.producer.first { it.streamName == streamName }
    private fun consumerStreamSettings(streamName: String) = kinesisProperties.consumer.first { it.streamName == streamName }

    private fun roleToAssume(streamProps: KinesisStream) = "arn:aws:iam::${streamProps.awsAccountId}:role/${streamProps.iamRoleToAssume}"
}

class ConsumerClientConfigFactory(private val credentialsProvider: AWSCredentialsProvider,
                                  private val kinesisProperties: AwsKinesisProperties) {

    fun consumerConfig(streamName: String, kinesisCredentials: AWSCredentialsProvider) = clientConfiguration(streamName, kinesisCredentials)
            .withInitialPositionInStream(TRIM_HORIZON)
            .withKinesisEndpoint(kinesisProperties.kinesisUrl)
            .withDynamoDBEndpoint(kinesisProperties.dynamodbUrl)
            .withMetricsLevel(kinesisProperties.metricsLevel)

    private fun clientConfiguration(streamName: String, kinesisCredentials: AWSCredentialsProvider) =
            KinesisClientLibConfiguration("${kinesisProperties.consumerGroup}_$streamName", streamName,
                    kinesisCredentials, credentialsProvider, credentialsProvider, workerId())

    private fun workerId() = InetAddress.getLocalHost().canonicalHostName + ":" + UUID.randomUUID()
}

class ProducerClientFactory(private val kinesisProperties: AwsKinesisProperties) {

    fun producer(credentials: AWSCredentialsProvider): AmazonKinesis {
        return AmazonKinesisClientBuilder.standard()
                .withCredentials(credentials)
                .withEndpointConfiguration(producerConfiguration())
                .build()
    }

    private fun producerConfiguration() = AwsClientBuilder.EndpointConfiguration(kinesisProperties.kinesisUrl, kinesisProperties.region)
}
