package mobi.waterdog.rest.template.tests.core.httphandler

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.identity
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.serialization.json
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import mobi.waterdog.rest.template.tests.core.model.Const
import mobi.waterdog.rest.template.tests.core.model.Expr
import mobi.waterdog.rest.template.tests.core.model.NotANumber
import mobi.waterdog.rest.template.tests.core.model.Sum
import mobi.waterdog.rest.template.tests.core.model.TestInstantLongSerialization
import mobi.waterdog.rest.template.tests.core.model.TestInstantStringSerialization
import mobi.waterdog.rest.template.tests.core.model.TestSealedClass
import mobi.waterdog.rest.template.tests.core.httphandler.testSerializationRoutes
import mobi.waterdog.rest.template.tests.core.utils.json.JsonSettings
import org.amshove.kluent.`should be after`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Instant
import java.time.format.DateTimeFormatter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSerializationRoutes {
    @Test
    fun `Testing serialization of sealed classes`() = testAppDefault<Unit> {
        fun eval(expr: Expr): Double = when (expr) {
            is Const -> expr.number
            is Sum -> eval(expr.e1) + eval(expr.e2)
            NotANumber -> Double.NaN
        }

        var expectedValue = 5.0
        with(handleRequest(HttpMethod.Get, "/sealed?type=const&value=$expectedValue")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: TestSealedClass = JsonSettings.fromJson(response.content)
            val double = when (res.exp) {
                is Const -> res.exp.number
                is Sum -> eval(res.exp.e1) + eval(res.exp.e2)
                NotANumber -> Double.NaN
                // the `else` clause is not required because we've covered all the cases
            }
            double `should be equal to` expectedValue
        }

        with(handleRequest(HttpMethod.Get, "/sealed?type=sum&value=2.5")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: TestSealedClass = JsonSettings.fromJson(response.content)
            val double = when (res.exp) {
                is Const -> res.exp.number
                is Sum -> eval(res.exp.e1) + eval(res.exp.e2)
                NotANumber -> Double.NaN
                // the `else` clause is not required because we've covered all the cases
            }
            double `should be equal to` expectedValue
        }

        expectedValue = Double.NaN
        with(handleRequest(HttpMethod.Get, "/sealed?type=other")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: TestSealedClass = JsonSettings.fromJson(response.content)
            val double = when (res.exp) {
                is Const -> res.exp.number
                is Sum -> eval(res.exp.e1) + eval(res.exp.e2)
                NotANumber -> Double.NaN
                // the `else` clause is not required because we've covered all the cases
            }
            double `should be equal to` expectedValue
        }
    }

    @Test
    fun `Testing instant class serialization`() = testAppDefault<Unit> {
        val now = Instant.now()

        // String Serialization Test
        val nowString = DateTimeFormatter.ISO_INSTANT.format(now)
        with(handleRequest(HttpMethod.Get, "/instant?type=string&time=$nowString")) {
            response.status() `should be equal to` HttpStatusCode.OK

            // Json serialized string should contain instant in its string format
            response.content?.shouldContain(nowString)
            val res: TestInstantStringSerialization = JsonSettings.fromJson(response.content)

            Instant.now() `should be after` res.date
            now `should be equal to` res.date
        }

        // Long Serialization Test
        val nowLong = now.toEpochMilli()
        with(handleRequest(HttpMethod.Get, "/instant?type=long&time=$nowLong")) {
            response.status() `should be equal to` HttpStatusCode.OK

            // Json serialized string should contain instant in its long format
            response.content?.shouldContain(nowLong.toString())
            val res: TestInstantLongSerialization = JsonSettings.fromJson(response.content)

            Instant.now() `should be after` res.date
            now.toEpochMilli() `should be equal to` res.date.toEpochMilli()
        }
    }
}

private fun <R> testAppDefault(test: TestApplicationEngine.() -> R) {
    withTestApplication(
        {
            module()
        },
        test
    )
}

fun Application.module() {
    install(DefaultHeaders)

    install(Compression) {
        gzip {
            priority = 100.0
        }
        identity {
            priority = 10.0
        }
        deflate {
            priority = 1.0
        }
    }

    install(ContentNegotiation) {
        json(
            contentType = ContentType.Application.Json,
            json = JsonSettings.mapper
        )
    }

    install(Routing) {
        testSerializationRoutes()
    }
}
