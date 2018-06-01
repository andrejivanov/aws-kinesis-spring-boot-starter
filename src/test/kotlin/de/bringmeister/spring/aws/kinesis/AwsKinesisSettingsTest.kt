package de.bringmeister.spring.aws.kinesis

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import de.bringmeister.spring.aws.kinesis.ConfigurationPropertiesBuilder.Companion.builder
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.validation.BindException
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [ValidationAutoConfiguration::class])
class AwsKinesisProducerSettingsTest {

    @Autowired
    private lateinit var localValidatorFactoryBean: LocalValidatorFactoryBean

    @Test
    fun `should read producer settings`() {

        val settings = builder<AwsKinesisSettings>()
            .populate(AwsKinesisSettings())
            .withPrefix("aws.kinesis")
            .validateUsing(localValidatorFactoryBean)
            .withProperty("aws.kinesis.region", "local")
            .withProperty("aws.kinesis.kinesis-url", "http://localhost:14567")
            .withProperty("aws.kinesis.producer[0].streamName", "foo-event-stream")
            .withProperty("aws.kinesis.producer[0].awsAccountId", "222222222222")
            .withProperty("aws.kinesis.producer[0].iamRoleToAssume", "ExampleKinesisProducerRole")
            .build()

        assertThat(settings.producer[0].streamName, equalTo("foo-event-stream"))
        assertThat(settings.producer[0].awsAccountId, equalTo("222222222222"))
        assertThat(settings.producer[0].iamRoleToAssume, equalTo("ExampleKinesisProducerRole"))
    }

    @Test
    fun `should read consumer settings`() {

        val settings = builder<AwsKinesisSettings>()
            .populate(AwsKinesisSettings())
            .withPrefix("aws.kinesis")
            .validateUsing(localValidatorFactoryBean)
            .withProperty("aws.kinesis.region", "local")
            .withProperty("aws.kinesis.kinesis-url", "http://localhost:14567")
            .withProperty("aws.kinesis.consumer[0].streamName", "foo-event-stream")
            .withProperty("aws.kinesis.consumer[0].awsAccountId", "111111111111")
            .withProperty("aws.kinesis.consumer[0].iamRoleToAssume", "ExampleKinesisConsumerRole")
            .build()

        assertThat(settings.consumer[0].streamName, equalTo("foo-event-stream"))
        assertThat(settings.consumer[0].awsAccountId, equalTo("111111111111"))
        assertThat(settings.consumer[0].iamRoleToAssume, equalTo("ExampleKinesisConsumerRole"))
    }

    @Test
    fun `should read default settings`() {

        val settings = builder<AwsKinesisSettings>()
            .populate(AwsKinesisSettings())
            .withPrefix("aws.kinesis")
            .withProperty("aws.kinesis.region", "local")
            .withProperty("aws.kinesis.kinesis-url", "http://localhost:14567")
            .validateUsing(localValidatorFactoryBean)
            .build()

        assertThat(settings.region, equalTo("local"))
        assertThat(settings.kinesisUrl, equalTo("http://localhost:14567"))
    }

    @Test(expected = BindException::class)
    fun `should fail if region is missing`() {
        builder<AwsKinesisSettings>()
            .populate(AwsKinesisSettings())
            .withPrefix("aws.kinesis")
            .validateUsing(localValidatorFactoryBean)
            .withProperty("aws.kinesis.kinesis-url", "http://localhost:14567")
            .build()
    }

    @Test(expected = BindException::class)
    fun `should fail if Kinesis URL is missing`() {
        builder<AwsKinesisSettings>()
            .populate(AwsKinesisSettings())
            .withPrefix("aws.kinesis")
            .validateUsing(localValidatorFactoryBean)
            .withProperty("aws.kinesis.region", "local")
            .build()
    }
}
