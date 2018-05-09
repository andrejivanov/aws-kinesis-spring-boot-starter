package de.bringmeister.spring.aws.kinesis

import org.springframework.aop.framework.Advised
import org.springframework.aop.support.AopUtils

class AopProxyUtils {

    companion object {

        fun unwrap(bean: Any): Any {
            val isAopProxy = AopUtils.isAopProxy(bean)
            return if (isAopProxy && bean is Advised) {
                bean.targetSource.target
            } else {
                bean
            }
        }
    }
}
