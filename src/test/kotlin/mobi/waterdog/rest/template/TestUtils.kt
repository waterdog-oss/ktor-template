package mobi.waterdog.rest.template

import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun <R> testApp(test: TestApplicationEngine.() -> R) {
    withTestApplication(
        {
            module()
        },
        test
    )
}
