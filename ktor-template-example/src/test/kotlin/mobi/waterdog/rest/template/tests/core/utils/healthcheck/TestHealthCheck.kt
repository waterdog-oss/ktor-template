package mobi.waterdog.rest.template.tests.core.utils.healthcheck

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import mobi.waterdog.rest.template.tests.containers.PgSQLContainerFactory
import mobi.waterdog.rest.template.tests.core.testApp
import mobi.waterdog.rest.template.tests.core.utils.json.JsonSettings
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.test.KoinTest
import org.slf4j.LoggerFactory
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration

@KtorExperimentalAPI
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestHealthCheck : KoinTest {
    companion object {
        @Container
        private val dbContainer = PgSQLContainerFactory.newInstance()
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    @Test
    fun `Checking liveness with 200 OK`() = testAppWithConfig {
        with(handleRequest(HttpMethod.Get, "/liveness")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val result: Map<String, Boolean> = JsonSettings.fromJson(response.content)
            result["alive"] `should be equal to` true
        }
    }

    @Test
    fun `Checking database readiness with 200 OK`() = testAppWithConfig {
        with(handleRequest(HttpMethod.Get, "/readiness")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val result: Map<String, Boolean> = JsonSettings.fromJson(response.content)
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
                val result: Map<String, Boolean> = JsonSettings.fromJson(response.content)
                result["database"] `should be equal to` true
            }

            dbContainer.stop()
            // The wait here is important to avoid timing issues
            log.info("Stopping container")
            waitFor(Duration.ofSeconds(3)) {
                !dbContainer.isRunning
            }
            log.info("Testing readiness. It should fail...")
            with(handleRequest(HttpMethod.Get, "/readiness")) {
                response.status() `should be equal to` HttpStatusCode.InternalServerError
                val result: Map<String, Boolean> = JsonSettings.fromJson(response.content)
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
                    Thread.sleep(100L.coerceAtMost(remaining))
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
