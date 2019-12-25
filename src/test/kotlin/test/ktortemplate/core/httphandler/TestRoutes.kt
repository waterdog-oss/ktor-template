package test.ktortemplate.core.httphandler

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.amshove.kluent.`should equal`
import org.junit.jupiter.api.Test
import test.ktortemplate.core.model.ResponseEntity
import test.ktortemplate.core.testApp
import test.ktortemplate.core.utils.JsonSettings

class TestRoutes {

    @Test
    fun `Test hello world`() = testApp<Unit> {
        with(handleRequest(HttpMethod.Get, "/hello")) {
            response.status() `should equal` HttpStatusCode.OK
            val response: ResponseEntity = JsonSettings.mapper.readValue(response.content!!)
            response.content `should equal` "Hello World"
        }
    }
}
