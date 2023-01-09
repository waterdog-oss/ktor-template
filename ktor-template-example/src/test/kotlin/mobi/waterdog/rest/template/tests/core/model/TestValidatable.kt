package mobi.waterdog.rest.template.tests.core.model

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import mobi.waterdog.rest.template.exception.AppException
import mobi.waterdog.rest.template.exception.ErrorDTO
import mobi.waterdog.rest.template.tests.containers.PgSQLContainerFactory
import mobi.waterdog.rest.template.tests.core.TestApplicationContext
import mobi.waterdog.rest.template.tests.core.testApp
import mobi.waterdog.rest.template.tests.core.utils.json.JsonSettings
import mobi.waterdog.rest.template.tests.core.utils.versioning.ApiVersion
import mobi.waterdog.rest.template.tests.module
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain any`
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.test.KoinTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

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
            client.post("/$apiVersion/cars") {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(JsonSettings.toJson(car))
            }
        ) {
            status `should be equal to` HttpStatusCode.OK
            val newCar: Car = body()
            newCar.id shouldNotBeEqualTo 0
        }
    }

    @Test
    fun `Posting a car with invalid wheels definition`() = testAppWithConfig {
        val car = CarSaveCommand("brand", "model", wheels = listOf(Wheel(0, 225)))

        with(
            client.post("/$apiVersion/cars") {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(JsonSettings.toJson(car))
            }
        ) {
            status `should be equal to` HttpStatusCode.BadRequest
            val error: ErrorDTO = body()
            error.httpStatusCode `should be equal to` HttpStatusCode.BadRequest.value
            error.messageCode `should be equal to` "client_error.invalid_parameters"
            error.errors.`should contain any` { it.errorCode.contains("in") }
            error.errors.`should contain any` { it.errorCode.contains("size") }
            error.errors.`should contain any` { it.errorCode.contains("wheels[0]") }
        }
    }

    private fun testAppWithConfig(test: suspend TestApplicationContext.() -> Unit) {
        testApp(
            {
                module(dbContainer.configInfo())
            },
            test
        )
    }
}
