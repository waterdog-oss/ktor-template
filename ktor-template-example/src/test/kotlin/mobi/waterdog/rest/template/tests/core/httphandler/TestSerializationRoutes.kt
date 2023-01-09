package mobi.waterdog.rest.template.tests.core.httphandler

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.identity
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.routing.Routing
import mobi.waterdog.rest.template.tests.core.TestApplicationContext
import mobi.waterdog.rest.template.tests.core.model.Const
import mobi.waterdog.rest.template.tests.core.model.Expr
import mobi.waterdog.rest.template.tests.core.model.NotANumber
import mobi.waterdog.rest.template.tests.core.model.Sum
import mobi.waterdog.rest.template.tests.core.model.TestInstantLongSerialization
import mobi.waterdog.rest.template.tests.core.model.TestInstantStringSerialization
import mobi.waterdog.rest.template.tests.core.model.TestSealedClass
import mobi.waterdog.rest.template.tests.core.testApp
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
    fun `Testing serialization of sealed classes`() = testAppDefault {
        fun eval(expr: Expr): Double = when (expr) {
            is Const -> expr.number
            is Sum -> eval(expr.e1) + eval(expr.e2)
            NotANumber -> Double.NaN
        }

        var expectedValue = 5.0
        with(client.get("/sealed?type=const&value=$expectedValue")) {
            status `should be equal to` HttpStatusCode.OK
            val res: TestSealedClass = body()
            val double = when (res.exp) {
                is Const -> res.exp.number
                is Sum -> eval(res.exp.e1) + eval(res.exp.e2)
                NotANumber -> Double.NaN
                // the `else` clause is not required because we've covered all the cases
            }
            double `should be equal to` expectedValue
        }

        with(client.get("/sealed?type=sum&value=2.5")) {
            status `should be equal to` HttpStatusCode.OK
            val res: TestSealedClass = body()
            val double = when (res.exp) {
                is Const -> res.exp.number
                is Sum -> eval(res.exp.e1) + eval(res.exp.e2)
                NotANumber -> Double.NaN
                // the `else` clause is not required because we've covered all the cases
            }
            double `should be equal to` expectedValue
        }

        expectedValue = Double.NaN
        with(client.get("/sealed?type=other")) {
            this.status `should be equal to` HttpStatusCode.OK
            val res: TestSealedClass = body()
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
    fun `Testing instant class serialization`() = testAppDefault {
        val now = Instant.now()

        // String Serialization Test
        val nowString = DateTimeFormatter.ISO_INSTANT.format(now)
        with(client.get("/instant?type=string&time=$nowString")) {
            status `should be equal to` HttpStatusCode.OK

            // Json serialized string should contain instant in its string format
            bodyAsText().shouldContain(nowString)
            val res: TestInstantStringSerialization = body()

            Instant.now() `should be after` res.date
            now `should be equal to` res.date
        }

        // Long Serialization Test
        val nowLong = now.toEpochMilli()
        with(client.get("/instant?type=long&time=$nowLong")) {
            status `should be equal to` HttpStatusCode.OK

            // Json serialized string should contain instant in its long format
            bodyAsText().shouldContain(nowLong.toString())
            val res: TestInstantLongSerialization = body()

            Instant.now() `should be after` res.date
            now.toEpochMilli() `should be equal to` res.date.toEpochMilli()
        }
    }
}

private fun testAppDefault(test: suspend TestApplicationContext.() -> Unit) {
    testApp(
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
