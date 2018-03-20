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
@EnableConfigurationProperties(AwsKinesisProperties::class)
class AwsKinesisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun kinesisClientProvider(consumerClientConfigFactory: ConsumerClientConfigFactory,
                              producerClientFactory: ProducerClientFactory,
                              kinesisCredentialsProviderFactory: AssumeRoleCredentialsProviderFactory,
                              kinesisProperties: AwsKinesisProperties) =

            AwsKinesisClientProvider(consumerClientConfigFactory, producerClientFactory, kinesisCredentialsProviderFactory, kinesisProperties)

    @Bean
    @ConditionalOnMissingBean
    fun kinesisConsumerClientConfigFactory(credentialsProvider: AWSCredentialsProvider,
                                           kinesisProperties: AwsKinesisProperties) =

            ConsumerClientConfigFactory(credentialsProvider, kinesisProperties)

    @Bean
    @ConditionalOnMissingBean
    fun kinesisProducerClientFactory(kinesisProperties: AwsKinesisProperties) = ProducerClientFactory(kinesisProperties)

    @Bean
    @ConditionalOnMissingBean
    fun credentialsProvider(properties: AwsKinesisProperties) = DefaultAWSCredentialsProviderChain() as AWSCredentialsProvider

    @Bean
    @ConditionalOnMissingBean
    fun assumeRoleCredentialsProviderFactory(kinesisProperties: AwsKinesisProperties,
                                             credentialsProvider: AWSCredentialsProvider): AssumeRoleCredentialsProviderFactory {
        return STSAssumeRoleCredentialsProviderFactory(credentialsProvider, kinesisProperties)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ObjectMapper::class)
    fun workerFactory(objectMapper: ObjectMapper) = WorkerFactory(objectMapper)

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ObjectMapper::class)
    fun requestFactory(objectMapper: ObjectMapper) = RequestFactory(objectMapper)

    @Bean
    @ConditionalOnMissingBean
    fun kinesisOutboundGateway(kinesisProperties: AwsKinesisProperties,
                               clientProvider: AwsKinesisClientProvider,
                               requestFactory: RequestFactory) = AwsKinesisOutboundGateway(kinesisProperties, clientProvider, requestFactory)

    @Bean
    @ConditionalOnMissingBean
    fun kinesisInboundGateway(clientProvider: AwsKinesisClientProvider,
                              workerFactory: WorkerFactory) = AwsKinesisInboundGateway(clientProvider, workerFactory)


}