package test.ktortemplate.core.httphandler

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.core.model.ResponseEntity
import test.ktortemplate.core.service.TestService

internal class DefaultRoutesrInjector : KoinComponent {
    val service: TestService by inject()
}

fun Route.defaultRoutes() {

    val injector = DefaultRoutesrInjector()
    val service = injector.service

    get("/hello") {
        call.respond(ResponseEntity(service.sayHello()))
    }
}
