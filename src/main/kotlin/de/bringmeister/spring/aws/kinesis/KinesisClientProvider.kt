package de.bringmeister.spring.aws.kinesis

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder

/**
 * This class will create AWS Kinesis clients for all streams configured
 * as "producers" in the "application.properties" / "application.yml". This
 * means, we can get a client for a specific stream in order to send messages
 * to this stream.
 */
class KinesisClientProvider(private val credentialFactory: AssumeRoleCredentialsProviderFactory,
                            private val kinesisSettings: AwsKinesisSettings) {

    private val kinesisClients : Map<String, AmazonKinesis>

    init {
        kinesisClients = kinesisSettings.producer.map { it.streamName to createClientFor(it.streamName) }.toMap()
    }

    fun clientFor(streamName: String): AmazonKinesis {
        return kinesisClients[streamName] ?: throw IllegalArgumentException("No client found for stream [${streamName}]")
    }

    private fun createClientFor(streamName: String): AmazonKinesis {
        val streamSettings = kinesisSettings.producer.first { it.streamName == streamName }
        val roleToAssume = "arn:aws:iam::${streamSettings.awsAccountId}:role/${streamSettings.iamRoleToAssume}"
        val credentials = credentialFactory.credentials(roleToAssume)
        return AmazonKinesisClientBuilder
                                    .standard()
                                    .withCredentials(credentials)
                                    .withEndpointConfiguration(
                                        AwsClientBuilder
                                            .EndpointConfiguration(kinesisSettings.kinesisUrl, kinesisSettings.region)
                                    )
                                    .build()
    }
}
