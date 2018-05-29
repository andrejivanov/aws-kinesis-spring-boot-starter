package de.bringmeister.spring.aws.kinesis.local

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import de.bringmeister.spring.aws.kinesis.AWSCredentialsProviderFactory
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
        override fun credentials(roleToAssume: String) = AWSStaticCredentialsProvider(BasicAWSCredentials("no-key", "no-passwd"))
    }
}