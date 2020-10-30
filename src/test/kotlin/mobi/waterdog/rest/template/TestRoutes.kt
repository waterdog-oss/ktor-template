package mobi.waterdog.rest.template

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestRoutes {

    @Test
    fun `Hello World test`() = testApp {
        with(handleRequest(HttpMethod.Get, "")) {
            response.status() `should be equal to` HttpStatusCode.OK
            response.content `should be equal to` "Hello World"
        }
    }
}
