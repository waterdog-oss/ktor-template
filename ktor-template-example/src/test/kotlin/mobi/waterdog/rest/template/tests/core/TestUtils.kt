package mobi.waterdog.rest.template.tests.core

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import mobi.waterdog.rest.template.tests.core.utils.json.JsonSettings

data class TestApplicationContext(
    val context: ApplicationTestBuilder,
    val client: HttpClient
)

fun <R> testApp(configure: Application.() -> R, test: suspend TestApplicationContext.() -> R) {
    testApplication {
        environment {
            config = MapApplicationConfig("ktor.environment" to "test")
        }

        application {
            configure(this)
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(
                    contentType = ContentType.Application.Json,
                    json = JsonSettings.mapper
                )
            }
        }

        test(TestApplicationContext(this, client))
    }
}
