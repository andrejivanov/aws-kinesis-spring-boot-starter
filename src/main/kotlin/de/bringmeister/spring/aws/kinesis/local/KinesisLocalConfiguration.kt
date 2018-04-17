package de.bringmeister.spring.aws.kinesis.local

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import de.bringmeister.spring.aws.kinesis.AWSCredentialsProviderFactory
import de.bringmeister.spring.aws.kinesis.AwsKinesisSettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("kinesis-local")
class KinesisLocalConfiguration {

    @Bean
    @Primary
    fun credentialsProvider() = AWSStaticCredentialsProvider(object : AWSCredentials {
        override fun getAWSAccessKeyId() = "no-key"
        override fun getAWSSecretKey() = "no-passwd"
    })

    @Bean
    @Primary
    fun kinesisCredentialsProvider() = object : AWSCredentialsProviderFactory {
        override fun credentials(roleToAssume: String) = AWSStaticCredentialsProvider(object : AWSCredentials {
            override fun getAWSAccessKeyId() = "no-key"
            override fun getAWSSecretKey() = "no-passwd"
        })
    }

    @Bean
    fun streamInitializer(kinesisSettings: AwsKinesisSettings): LocalAwsKinesisStreamInitializer {
        System.setProperty("com.amazonaws.sdk.disableCbor", "1")

        val initializer = LocalAwsKinesisStreamInitializer(AmazonKinesisClientBuilder.standard()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(kinesisSettings.kinesisUrl, "local"))
                .withCredentials(credentialsProvider())
                .build())

        kinesisSettings.consumer
                .map { it.streamName }
                .forEach { it -> initializer.initStream(it) }

        kinesisSettings.producer
                .map { it.streamName }
                .forEach { it -> initializer.initStream(it) }

        return initializer
    }
}