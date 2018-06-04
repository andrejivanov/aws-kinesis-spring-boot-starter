package de.bringmeister.spring.aws.kinesis

import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotNull

@Validated
@ConfigurationProperties(prefix = "aws.kinesis")
class AwsKinesisSettings {

    @NotNull
    lateinit var region: String // Example: eu-central-1, local
    lateinit var awsAccountId: String // Example: 123456789012
    lateinit var iamRoleToAssume: String // Example: role_name
    lateinit var consumerGroup: String // Example: my-service

    var kinesisUrl: String? = null // Example: http://localhost:14567
        get() {
            return field ?: return if (::region.isInitialized) {
                "https://kinesis.$region.amazonaws.com"
            } else {
                return null
            }
        }

    var dynamoDbSettings: DynamoDbSettings? = null
        get() {
            return field ?: return if (::region.isInitialized) {
                val settings = DynamoDbSettings()
                settings.url = "https://dynamodb.$region.amazonaws.com"
                return settings
            } else {
                null
            }
        }

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
        defaultSettings.awsAccountId = awsAccountId
        defaultSettings.iamRoleToAssume = iamRoleToAssume
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
