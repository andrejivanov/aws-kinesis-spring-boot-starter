package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import org.springframework.boot.context.properties.ConfigurationProperties
import javax.validation.constraints.NotNull

@ConfigurationProperties(prefix = "aws.kinesis")
class AwsKinesisSettings {

    @NotNull
    lateinit var region: String // Example: eu-central-1, local

    @NotNull
    lateinit var kinesisUrl: String // Example: https://kinesis.eu-central-1.amazonaws.com, http://localhost:14567

    var awsAccountId: String? = null // Example: 123456789012
    var iamRoleToAssume: String? = null // Example: role_name
    var consumerGroup: String? = null // Example: my-service
    var dynamoDbSettings: DynamoDbSettings? = null
    var metricsLevel = MetricsLevel.NONE.name
    var createStreams: Boolean = false
    var creationTimeout: Int = 30
    var consumer: MutableList<StreamSettings> = mutableListOf()
    var producer: MutableList<StreamSettings> = mutableListOf()

    fun getConsumerSettingsOrDefault(stream: String): StreamSettings {
        return consumer.firstOrNull { it.streamName == stream } ?: return defaultSettingsFor(stream)
    }

    fun getProducerSettingsOrDefault(stream: String): StreamSettings {
        return producer.firstOrNull { it.streamName == stream } ?: return defaultSettingsFor(stream)
    }

    private fun defaultSettingsFor(stream: String): StreamSettings {
        val defaultSettings = StreamSettings()
        defaultSettings.streamName = stream
        defaultSettings.awsAccountId = awsAccountId!!
        defaultSettings.iamRoleToAssume = iamRoleToAssume!!
        return defaultSettings
    }
}

class DynamoDbSettings {

    @NotNull
    lateinit var url: String // https://dynamodb.eu-central-1.amazonaws.com

    var leaseTableReadCapacity = 1
    var leaseTableWriteCapacity = 1
}

class StreamSettings {

    @NotNull
    lateinit var streamName: String

    @NotNull
    lateinit var awsAccountId: String

    @NotNull
    lateinit var iamRoleToAssume: String
}
