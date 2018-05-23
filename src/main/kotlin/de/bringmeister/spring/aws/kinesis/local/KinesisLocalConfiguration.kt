package de.bringmeister.spring.aws.kinesis.local

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.AnonymousAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder.*
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
    fun kinesisCredentialsProvider() = object : AWSCredentialsProviderFactory {
        override fun credentials(roleToAssume: String) = AWSStaticCredentialsProvider(object : AWSCredentials {
            override fun getAWSAccessKeyId() = "no-key"
            override fun getAWSSecretKey() = "no-passwd"
        })
    }

    @Bean
    fun streamInitializer(kinesisSettings: AwsKinesisSettings): KinesisStreamInitializer {
        System.setProperty("com.amazonaws.sdk.disableCbor", "1")

        val endpoint = AwsClientBuilder.EndpointConfiguration(kinesisSettings.kinesisUrl, "local")
        val credentials = AWSStaticCredentialsProvider(object : AWSCredentials {
            override fun getAWSAccessKeyId() = "no-key"
            override fun getAWSSecretKey() = "no-passwd"
        })
        val kinesisClient = standard().withEndpointConfiguration(endpoint).withCredentials(credentials).build()
        val initializer = KinesisStreamInitializer(kinesisClient)

        kinesisSettings.consumer
            .map { it.streamName }
            .forEach { it -> initializer.createStreamIfMissing(it) }

        kinesisSettings.producer
            .map { it.streamName }
            .forEach { it -> initializer.createStreamIfMissing(it) }

        return initializer
    }
}