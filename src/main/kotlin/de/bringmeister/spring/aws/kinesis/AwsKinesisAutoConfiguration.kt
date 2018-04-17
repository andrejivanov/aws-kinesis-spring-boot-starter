package de.bringmeister.spring.aws.kinesis

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureAfter(name = ["org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration"])
@EnableConfigurationProperties(AwsKinesisSettings::class)
class AwsKinesisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun clientConfigFactory(credentialsProvider: AWSCredentialsProvider,
                            kinesisCredentialsProviderFactory: AssumeRoleCredentialsProviderFactory,
                            kinesisSettings: AwsKinesisSettings) : ClientConfigFactory {

        return ClientConfigFactory(credentialsProvider, kinesisCredentialsProviderFactory, kinesisSettings)
    }

    @Bean
    @ConditionalOnMissingBean
    fun credentialsProvider(settings: AwsKinesisSettings) = DefaultAWSCredentialsProviderChain() as AWSCredentialsProvider

    @Bean
    @ConditionalOnMissingBean
    fun assumeRoleCredentialsProviderFactory(kinesisSettings: AwsKinesisSettings,
                                             credentialsProvider: AWSCredentialsProvider): AssumeRoleCredentialsProviderFactory {
        return STSAssumeRoleCredentialsProviderFactory(credentialsProvider, kinesisSettings)
    }

    @Bean
    @ConditionalOnMissingBean
    fun workerStarter() = WorkerStarter()

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ObjectMapper::class)
    fun workerFactory(clientConfigFactory: ClientConfigFactory, objectMapper: ObjectMapper) = WorkerFactory(clientConfigFactory, objectMapper)

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ObjectMapper::class)
    fun requestFactory(objectMapper: ObjectMapper) = RequestFactory(objectMapper)

    @Bean
    @ConditionalOnMissingBean
    fun kinesisClientProvider(awsKinesisSettings: AwsKinesisSettings,
                              assumeRoleCredentialsProviderFactory: AssumeRoleCredentialsProviderFactory) = KinesisClientProvider(assumeRoleCredentialsProviderFactory, awsKinesisSettings)

    @Bean
    @ConditionalOnMissingBean
    fun kinesisOutboundGateway(kinesisClientProvider: KinesisClientProvider,
                               requestFactory: RequestFactory) = AwsKinesisOutboundGateway(kinesisClientProvider, requestFactory)

    @Bean
    @ConditionalOnMissingBean
    fun kinesisInboundGateway(workerFactory: WorkerFactory,
                              workerStarter: WorkerStarter) = AwsKinesisInboundGateway(workerFactory, workerStarter)
}