package de.bringmeister.spring.aws.kinesis

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.aop.support.AopUtils.isAopProxy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [AopProxyUtilsTest.TestService::class])
class AopProxyUtilsTest {

    @Autowired
    lateinit var testService: TestService

    @Test
    fun `should return proxied object`() {
        val unwrapped = AopProxyUtils().unwrap(testService)

        assertThat(isAopProxy(testService)).isTrue()
        assertThat(isAopProxy(unwrapped)).isFalse()
        assertThat(testService).isNotEqualTo(unwrapped)
    }

    @Test
    fun `should return original object if it's not proxied`() {
        val string = "I'm not a proxy"
        val unwrapped = AopProxyUtils().unwrap(string)

        assertThat(isAopProxy(string)).isFalse()
        assertThat(isAopProxy(unwrapped)).isFalse()
        assertThat(string).isEqualTo(unwrapped)
    }

    // This class will be wrapped in a proxy by Spring.
    @Service
    @Scope("singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
    class TestService
}
