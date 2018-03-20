package de.bringmeister.spring.aws.kinesis.local

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import de.bringmeister.spring.aws.kinesis.AssumeRoleCredentialsProviderFactory
import de.bringmeister.spring.aws.kinesis.AwsKinesisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("kinesis-local")
class KinesisLocalConfiguration {

    @Bean
    @Primary
    fun credentials() = AWSStaticCredentialsProvider(object : AWSCredentials {
        override fun getAWSAccessKeyId() = "no-key"
        override fun getAWSSecretKey() = "no-passwd"
    })

    @Bean
    @Primary
    fun kinesisCredentialsProvider() = object : AssumeRoleCredentialsProviderFactory {
        override fun credentials(roleToAssume: String) = AWSStaticCredentialsProvider(object : AWSCredentials {
            override fun getAWSAccessKeyId() = "no-key"
            override fun getAWSSecretKey() = "no-passwd"
        })
    }

    @Bean
    fun streamInitializer(kinesisProperties: AwsKinesisProperties): LocalAwsKinesisStreamInitializer {
        System.setProperty("com.amazonaws.sdk.disableCbor", "1")

        val initializer = LocalAwsKinesisStreamInitializer(AmazonKinesisClientBuilder.standard()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(kinesisProperties.kinesisUrl, "local"))
                .withCredentials(credentials())
                .build())

        kinesisProperties.consumer
                .map { it.streamName }
                .forEach { it -> initializer.initStream(it) }

        kinesisProperties.producer
                .map { it.streamName }
                .forEach { it -> initializer.initStream(it) }

        return initializer
    }
}