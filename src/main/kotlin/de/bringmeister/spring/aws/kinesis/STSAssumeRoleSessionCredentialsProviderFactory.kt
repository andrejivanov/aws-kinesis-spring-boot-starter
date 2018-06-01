package de.bringmeister.spring.aws.kinesis

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import java.util.UUID

class STSAssumeRoleSessionCredentialsProviderFactory(
    private val credentialsProvider: AWSCredentialsProvider,
    private val settings: AwsKinesisSettings
) : AWSCredentialsProviderFactory {

    override fun credentials(roleToAssume: String): AWSCredentialsProvider {
        return STSAssumeRoleSessionCredentialsProvider
            .Builder(roleToAssume, UUID.randomUUID().toString())
            .withStsClient(
                AWSSecurityTokenServiceClientBuilder
                    .standard()
                    .withRegion(settings.region)
                    .withCredentials(credentialsProvider)
                    .build()
            )
            .build()
    }
}