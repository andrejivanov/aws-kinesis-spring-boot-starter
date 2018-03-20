package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import org.springframework.boot.context.properties.ConfigurationProperties
import javax.validation.constraints.NotNull

@ConfigurationProperties(prefix = "aws.kinesis")
class AwsKinesisProperties {

    var region = "eu-central-1"
    var kinesisUrl = "https://kinesis.eu-central-1.amazonaws.com"
    var dynamodbUrl = "https://dynamodb.eu-central-1.amazonaws.com"

    var metricsLevel = MetricsLevel.NONE.name

    @NotNull
    lateinit var consumerGroup: String

    @NotNull
    var consumer: MutableList<KinesisStream> = mutableListOf()
    @NotNull
    var producer: MutableList<KinesisStream> = mutableListOf()

}

class KinesisStream {
    @NotNull
    lateinit var streamName: String
    @NotNull
    lateinit var awsAccountId: String
    @NotNull
    lateinit var iamRoleToAssume: String
}
