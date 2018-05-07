package de.bringmeister.spring.aws.kinesis

import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class KinesisListenerPostProcessor(private val kinesisOutboundGateway: AwsKinesisInboundGateway) : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any {
        return bean!! // nothing to do in this case
    }

    @Throws(BeansException::class)
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {

        KinesisListenerProxyFactory()
            .proxiesFor(bean)
            .forEach(kinesisOutboundGateway::register)

        return bean
    }
}