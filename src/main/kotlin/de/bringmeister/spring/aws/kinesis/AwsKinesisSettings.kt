package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import org.springframework.boot.context.properties.ConfigurationProperties
import javax.validation.constraints.NotNull

@ConfigurationProperties(prefix = "aws.kinesis")
class AwsKinesisSettings {

    var region = "eu-central-1"
    var kinesisUrl = "https://kinesis.eu-central-1.amazonaws.com"

    @NotNull
    lateinit var consumerGroup: String

    @NotNull
    var consumer: MutableList<ConsumerSettings> = mutableListOf()

    @NotNull
    var producer: MutableList<ProducerSettings> = mutableListOf()
}

open class StreamSettings {
    @NotNull
    lateinit var streamName: String

    @NotNull
    lateinit var awsAccountId: String

    @NotNull
    lateinit var iamRoleToAssume: String
}

class ConsumerSettings : StreamSettings() {
    var dynamoDBSettings = DynamoDBSettings()
    var metricsLevel = MetricsLevel.NONE.name
}

class DynamoDBSettings {
    var url = "https://dynamodb.eu-central-1.amazonaws.com"
    var leaseTableReadCapacity = 1
    var leaseTableWriteCapacity = 1
}

class ProducerSettings : StreamSettings()