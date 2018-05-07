package de.bringmeister.spring.aws.kinesis

import java.lang.reflect.Method

data class KinesisListenerProxy(val method: Method,
                                val bean: Any,
                                val stream: String) {

    fun invoke(data: Any?, metadata: Any?) {
        method.invoke(bean, data, metadata)
    }
}
