package test.ktortemplate.core.model

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain any`
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.test.KoinTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import test.ktortemplate.containers.PgSQLContainerFactory
import test.ktortemplate.core.exception.AppException
import test.ktortemplate.core.exception.ErrorDTO
import test.ktortemplate.core.testApp
import test.ktortemplate.core.utils.json.JsonSettings
import test.ktortemplate.core.utils.versioning.ApiVersion

@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class TestValidatable : KoinTest {

    companion object {
        @Container
        private val dbContainer = PgSQLContainerFactory.newInstance()
    }

    private val apiVersion = ApiVersion.Latest

    @Test
    fun `Validating a car with an invalid brand`() {
        assertThrows<AppException> {
            Car(1, "ford", "Focus").validate()
        }
    }

    @Test
    fun `Validating a car with a valid brand`() {
        Car(1, "koenigsegg", "cc").validate()
    }

    @Test
    fun `Validate a list a with an invalid wheel definition`() {
        val wheels = listOf(
            Wheel(17, 225),
            Wheel(17, 255),
            Wheel(3, 225) // <-- invalid diameter
        )
        assertThrows<AppException> {
            Car(1, "porsche", "911", wheels).validate()
        }
    }

    @Test
    fun `Posting a car with success`() = testAppWithConfig {
        val car = CarSaveCommand(
            "porsche",
            "911",
            listOf(Wheel(15, 195), Wheel(15, 195), Wheel(15, 195))
        )

        with(
            handleRequest(HttpMethod.Post, "/$apiVersion/cars") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(JsonSettings.toJson(car))
            }
        ) {
            response.status() `should be equal to` HttpStatusCode.OK
            val newCar: Car = JsonSettings.fromJson(response.content)
            newCar.id shouldNotBeEqualTo 0
        }
    }

    @Test
    fun `Posting a car with invalid wheels definition`() = testAppWithConfig {
        val car = CarSaveCommand("brand", "model", wheels = listOf(Wheel(0, 225)))

        with(
            handleRequest(HttpMethod.Post, "/$apiVersion/cars") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(JsonSettings.toJson(car))
            }
        ) {
            response.status() `should be equal to` HttpStatusCode.BadRequest
            val error: ErrorDTO = JsonSettings.fromJson(response.content)
            error.httpStatusCode `should be equal to` HttpStatusCode.BadRequest.value
            error.messageCode `should be equal to` "client_error.invalid_parameters"
            error.errors.`should contain any` { it.errorCode.contains("in") }
            error.errors.`should contain any` { it.errorCode.contains("size") }
            error.errors.`should contain any` { it.errorCode.contains("wheels[0]") }
        }
    }

    private fun <R> testAppWithConfig(test: TestApplicationEngine.() -> R) {
        testApp(dbContainer.configInfo(), test)
    }
}
