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
                            awsCredentialsProviderFactory: AWSCredentialsProviderFactory,
                            kinesisSettings: AwsKinesisSettings) : ClientConfigFactory {

        return ClientConfigFactory(credentialsProvider, awsCredentialsProviderFactory, kinesisSettings)
    }

    @Bean
    @ConditionalOnMissingBean
    fun credentialsProvider(settings: AwsKinesisSettings) = DefaultAWSCredentialsProviderChain() as AWSCredentialsProvider

    @Bean
    @ConditionalOnMissingBean
    fun credentialsProviderFactory(kinesisSettings: AwsKinesisSettings,
                                   credentialsProvider: AWSCredentialsProvider): AWSCredentialsProviderFactory {

        return STSAssumeRoleSessionCredentialsProviderFactory(credentialsProvider, kinesisSettings)
    }

    @Bean
    @ConditionalOnMissingBean
    fun workerStarter() = WorkerStarter()

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ObjectMapper::class)
    fun recordMapper(objectMapper: ObjectMapper): RecordMapper {
        return ReflectionBasedRecordMapper(objectMapper)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ObjectMapper::class)
    fun workerFactory(clientConfigFactory: ClientConfigFactory, recordMapper: RecordMapper) = WorkerFactory(clientConfigFactory, recordMapper)

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ObjectMapper::class)
    fun requestFactory(objectMapper: ObjectMapper) = RequestFactory(objectMapper)

    @Bean
    @ConditionalOnMissingBean
    fun kinesisClientProvider(awsKinesisSettings: AwsKinesisSettings,
                              awsCredentialsProviderFactory: AWSCredentialsProviderFactory) = KinesisClientProvider(awsCredentialsProviderFactory, awsKinesisSettings)

    @Bean
    @ConditionalOnMissingBean
    fun kinesisOutboundGateway(kinesisClientProvider: KinesisClientProvider,
                               requestFactory: RequestFactory) = AwsKinesisOutboundGateway(kinesisClientProvider, requestFactory)

    @Bean
    @ConditionalOnMissingBean
    fun kinesisInboundGateway(workerFactory: WorkerFactory,
                              workerStarter: WorkerStarter) = AwsKinesisInboundGateway(workerFactory, workerStarter)

    @Bean
    @ConditionalOnMissingBean
    fun kinesisListenerPostProcessor(inboundGateway: AwsKinesisInboundGateway): KinesisListenerPostProcessor {
        return KinesisListenerPostProcessor(inboundGateway)
    }
}