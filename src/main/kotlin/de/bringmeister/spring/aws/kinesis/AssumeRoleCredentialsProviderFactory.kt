package de.bringmeister.spring.aws.kinesis

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import java.util.*

interface AssumeRoleCredentialsProviderFactory {
    fun credentials(roleToAssume: String): AWSCredentialsProvider
}

internal class STSAssumeRoleCredentialsProviderFactory(private val credentialsProvider: AWSCredentialsProvider,
                                                       private val properties: AwsKinesisProperties) : AssumeRoleCredentialsProviderFactory {

    override fun credentials(roleToAssume: String) = STSAssumeRoleSessionCredentialsProvider
            .Builder(roleToAssume, "${properties.consumerGroup}-${UUID.randomUUID()}")
            .withStsClient(AWSSecurityTokenServiceClientBuilder.standard()
                    .withRegion(properties.region)
                    .withCredentials(credentialsProvider)
                    .build())
            .build()
}