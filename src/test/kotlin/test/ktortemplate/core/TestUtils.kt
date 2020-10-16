package test.ktortemplate.core

import io.ktor.config.ApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import test.ktortemplate.module

@KtorExperimentalAPI
fun <R> testApp(testConfig: ApplicationConfig, test: TestApplicationEngine.() -> R) {
    withTestApplication(
        {
            module(testConfig)
        },
        test
    )
}
