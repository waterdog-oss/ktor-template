package test.ktortemplate.core.utils.healthcheck

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.test.KoinTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import test.ktortemplate.containers.PgSQLContainerFactory
import test.ktortemplate.core.testApp
import test.ktortemplate.core.utils.JsonSettings

@KtorExperimentalAPI
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestHealthCheck : KoinTest {

    companion object {
        @Container
        private val dbContainer = PgSQLContainerFactory.newInstance()
    }

    @Test
    fun `Checking liveness with 200OK`() = testAppWithConfig {
        with(handleRequest(HttpMethod.Get, "/liveness")) {
            response.status() `should be equal to` io.ktor.http.HttpStatusCode.OK
            val result: Map<String, String> = JsonSettings.mapper.readValue(response.content!!)
            result["alive"].toBoolean() `should be equal to` true
        }
    }

    @Test
    fun `Checking database readiness with 200OK`() = testAppWithConfig {
        with(handleRequest(HttpMethod.Get, "/readiness")) {
            response.status() `should be equal to` io.ktor.http.HttpStatusCode.OK
            val result: Map<String, String> = JsonSettings.mapper.readValue(response.content!!)
            result["database"].toBoolean() `should be equal to` true
        }
    }

    private fun <R> testAppWithConfig(test: TestApplicationEngine.() -> R) {
        testApp(dbContainer.configInfo(), test)
    }
}
