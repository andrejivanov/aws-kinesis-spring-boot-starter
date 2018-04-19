package de.bringmeister.spring.aws.kinesis

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream.TRIM_HORIZON
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration
import java.net.InetAddress
import java.util.UUID

class ClientConfigFactory(private val credentialsProvider: AWSCredentialsProvider,
                          private val awsCredentialsProviderFactory: AWSCredentialsProviderFactory,
                          private val kinesisSettings: AwsKinesisSettings) {

    fun consumerConfig(streamName: String): KinesisClientLibConfiguration {

        val consumerSettings = kinesisSettings.consumer.first { it.streamName == streamName }
        val roleToAssume = "arn:aws:iam::${consumerSettings.awsAccountId}:role/${consumerSettings.iamRoleToAssume}"
        val credentials = awsCredentialsProviderFactory.credentials(roleToAssume)
        val workerId = InetAddress.getLocalHost().canonicalHostName + ":" + UUID.randomUUID()
        val applicationName = "${kinesisSettings.consumerGroup}_$streamName"

        return KinesisClientLibConfiguration(applicationName, streamName, credentials, credentialsProvider, credentialsProvider, workerId)
                            .withInitialPositionInStream(TRIM_HORIZON)
                            .withKinesisEndpoint(kinesisSettings.kinesisUrl)
                            .withDynamoDBEndpoint(consumerSettings.dynamoDBSettings.url)
                            .withMetricsLevel(consumerSettings.metricsLevel)
                            .withInitialLeaseTableReadCapacity(consumerSettings.dynamoDBSettings.leaseTableReadCapacity)
                            .withInitialLeaseTableWriteCapacity(consumerSettings.dynamoDBSettings.leaseTableWriteCapacity)
    }
}