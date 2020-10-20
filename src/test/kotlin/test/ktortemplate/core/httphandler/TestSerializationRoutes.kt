package test.ktortemplate.core.httphandler

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.amshove.kluent.`should be after`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import test.ktortemplate.core.initDbCore
import test.ktortemplate.core.initServicesAndRepos
import test.ktortemplate.core.model.Const
import test.ktortemplate.core.model.Expr
import test.ktortemplate.core.model.NotANumber
import test.ktortemplate.core.model.Sum
import test.ktortemplate.core.model.TestInstantSerialization
import test.ktortemplate.core.model.TestSealedClass
import test.ktortemplate.core.testApp
import test.ktortemplate.core.utils.json.JsonSettings
import java.time.Instant

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSerializationRoutes : KoinTest {

    @BeforeAll
    fun setup() {
        val appModules = listOf(
            initDbCore(),
            initServicesAndRepos()
        )
        startKoin { modules(appModules) }
    }

    @Test
    fun `Testing serialization of sealed classes`() = testApp<Unit> {
        fun eval(expr: Expr): Double = when (expr) {
            is Const -> expr.number
            is Sum -> eval(expr.e1) + eval(expr.e2)
            is NotANumber -> Double.NaN
        }

        var expectedValue = 5.0
        with(handleRequest(HttpMethod.Get, "/sealed?type=const&value=$expectedValue")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: TestSealedClass = JsonSettings.fromJson(response.content)
            val double = when (res.exp) {
                is Const -> (res.exp as Const).number
                is Sum -> eval((res.exp as Sum).e1) + eval((res.exp as Sum).e2)
                is NotANumber -> Double.NaN
                // the `else` clause is not required because we've covered all the cases
            }
            double `should be equal to` expectedValue
        }

        with(handleRequest(HttpMethod.Get, "/sealed?type=sum&value=2.5")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: TestSealedClass = JsonSettings.fromJson(response.content)
            val double = when (res.exp) {
                is Const -> (res.exp as Const).number
                is Sum -> eval((res.exp as Sum).e1) + eval((res.exp as Sum).e2)
                is NotANumber -> Double.NaN
                // the `else` clause is not required because we've covered all the cases
            }
            double `should be equal to` expectedValue
        }

        expectedValue = Double.NaN
        with(handleRequest(HttpMethod.Get, "/sealed?type=other")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: TestSealedClass = JsonSettings.fromJson(response.content)
            val double = when (res.exp) {
                is Const -> (res.exp as Const).number
                is Sum -> eval((res.exp as Sum).e1) + eval((res.exp as Sum).e2)
                is NotANumber -> Double.NaN
                // the `else` clause is not required because we've covered all the cases
            }
            double `should be equal to` expectedValue
        }
    }

    @Test
    fun `Testing instant class serialization`() = testApp<Unit> {
        val now = Instant.now()

        // String Serialization Test
        val nowLong = now.toEpochMilli()
        with(handleRequest(HttpMethod.Get, "/instant?time=$nowLong")) {
            response.status() `should be equal to` HttpStatusCode.OK

            // Json serialized string should contain instant in its long format
            response.content?.shouldContain(nowLong.toString())
            val res: TestInstantSerialization = JsonSettings.fromJson(response.content)

            Instant.now() `should be after` res.date
            now.toEpochMilli() `should be equal to` res.date.toEpochMilli()
        }
    }
}
