package mobi.waterdog.rest.template.tests.core

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import mobi.waterdog.rest.template.tests.module

fun <R> testApp(testConfig: ApplicationConfig, test: TestApplicationEngine.() -> R) {
    withTestApplication(
        {
            module(testConfig)
        },
        test
    )
}
