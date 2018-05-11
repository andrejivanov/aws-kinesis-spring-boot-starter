package de.bringmeister.spring.aws.kinesis

import org.springframework.aop.framework.Advised
import org.springframework.aop.support.AopUtils

/**
 * This class is a factory for [KinesisListenerProxy]. It takes any object and
 * looks for methods annotated with [KinesisListener]. For any of these methods
 * a [KinesisListenerProxy] is created. If no methods annotated with [KinesisListener]
 * are found, an empty list will be returned.
 */
class KinesisListenerProxyFactory {

    fun proxiesFor(bean: Any) : List<KinesisListenerProxy> {

        // Since we are in a Sprint environment, it's very likely that
        // we don't receive plain objects but AOP proxies. In order to
        // work properly on those proxies, we need to "unwrap" them.
        val isAopProxy = AopUtils.isAopProxy(bean)
        val objectToProcess = if (isAopProxy && bean is Advised) {
            bean.targetSource.target;
        } else {
            bean
        }

        return objectToProcess
                .javaClass
                .methods
                .filter({ method -> method.isAnnotationPresent(KinesisListener::class.java) })
                // the original bean! not the objectToProcess!
                .map({
                    method -> KinesisListenerProxy(method,
                                                   bean,
                                                   method.getAnnotation(KinesisListener::class.java).stream,
                                                   method.getAnnotation(KinesisListener::class.java).threadPoolSize)
                })
    }
}
