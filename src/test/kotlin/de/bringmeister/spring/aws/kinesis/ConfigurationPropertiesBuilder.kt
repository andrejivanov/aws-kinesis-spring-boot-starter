package de.bringmeister.spring.aws.kinesis

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.boot.bind.PropertiesConfigurationFactory
import org.springframework.core.env.MutablePropertySources
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.io.ClassPathResource
import org.springframework.validation.Validator
import java.util.ArrayList
import java.util.Properties

class ConfigurationPropertiesBuilder<T> {

    private var `object`: T? = null
    private var fileName: String? = null
    private var prefix: String? = null
    private var validator: Validator? = null
    private val properties = Properties()
    private val propertiesToRemove = ArrayList<String>()

    companion object {
        fun <T> builder(): ConfigurationPropertiesBuilder<T> {
            return ConfigurationPropertiesBuilder()
        }
    }

    fun populate(`object`: T): ConfigurationPropertiesBuilder<T> {
        this.`object` = `object`
        return this
    }

    fun fromFile(fileName: String): ConfigurationPropertiesBuilder<T> {
        this.fileName = fileName
        return this
    }

    fun withPrefix(prefix: String): ConfigurationPropertiesBuilder<T> {
        this.prefix = prefix
        return this
    }

    fun validateUsing(validator: Validator): ConfigurationPropertiesBuilder<T> {
        this.validator = validator
        return this
    }

    fun withProperty(key: String, value: String): ConfigurationPropertiesBuilder<T> {
        properties.setProperty(key, value)
        return this
    }

    fun withoutProperty(key: String): ConfigurationPropertiesBuilder<T> {
        propertiesToRemove.add(key)
        return this
    }

    fun build(): T {

        val propertiesFromFile = loadYamlProperties(fileName)
        propertiesToRemove.forEach({ properties.remove(it) })
        propertiesToRemove.forEach({ propertiesFromFile.remove(it) })

        val propertySources = MutablePropertySources()
        propertySources.addLast(PropertiesPropertySource("properties", properties))
        propertySources.addLast(PropertiesPropertySource("propertiesFromFile", propertiesFromFile))

        val configurationFactory = PropertiesConfigurationFactory(`object`)
        configurationFactory.setPropertySources(propertySources)
        configurationFactory.setTargetName(prefix)
        configurationFactory.setValidator(validator)
        configurationFactory.bindPropertiesToTarget()

        return `object` as T
    }

    private fun loadYamlProperties(fileName: String?): Properties {
        if (fileName == null) {
            return Properties()
        }
        val resource = ClassPathResource(fileName)
        val factoryBean = YamlPropertiesFactoryBean()
        factoryBean.setResources(resource)
        return factoryBean.`object`
    }
}
