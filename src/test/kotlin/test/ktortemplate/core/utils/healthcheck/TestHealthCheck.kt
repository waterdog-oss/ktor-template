package test.ktortemplate.core.utils.healthcheck

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
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
import java.time.Duration

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
            response.status() `should be equal to` HttpStatusCode.OK
            val result: Map<String, Boolean> = JsonSettings.mapper.readValue(response.content!!)
            result["alive"] `should be equal to` true
        }
    }

    @Test
    fun `Checking database readiness with 200OK`() = testAppWithConfig {
        with(handleRequest(HttpMethod.Get, "/readiness")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val result: Map<String, Boolean> = JsonSettings.mapper.readValue(response.content!!)
            result["database"] `should be equal to` true
        }
    }

    @Test
    fun `Checking readiness with 500 Internal Server Error`() {
        val dbContainer = PgSQLContainerFactory.newInstance()
        dbContainer.start()
        waitFor(Duration.ofSeconds(3)) {
            dbContainer.isRunning
        }
        testApp(dbContainer.configInfo()) {
            with(handleRequest(HttpMethod.Get, "/readiness")) {
                response.status() `should be equal to` HttpStatusCode.OK
                val result: Map<String, String> = JsonSettings.mapper.readValue(response.content!!)
                result["database"].toBoolean() `should be equal to` true
            }

            dbContainer.stop()
            with(handleRequest(HttpMethod.Get, "/readiness")) {
                response.status() `should be equal to` HttpStatusCode.InternalServerError
                val result: Map<String, Boolean> = JsonSettings.mapper.readValue(response.content!!)
                result["database"] `should be equal to` false
            }
        }
    }

    private fun <R> testAppWithConfig(test: TestApplicationEngine.() -> R) {
        testApp(dbContainer.configInfo(), test)
    }

    private fun waitFor(duration: Duration, block: () -> Boolean) {
        val finishAt = System.currentTimeMillis() + duration.toMillis()
        var wasInterrupted = false
        return try {
            while (!block()) {
                val remaining = finishAt - System.currentTimeMillis()
                if (remaining < 0L) {
                    throw IllegalStateException("Exceeded the time limit for condition")
                }
                try {
                    Thread.sleep(Math.min(100L, remaining))
                } catch (interrupt: InterruptedException) {
                    wasInterrupted = true
                }
            }
        } finally {
            if (wasInterrupted) {
                Thread.currentThread().interrupt()
            }
        }
    }
}
