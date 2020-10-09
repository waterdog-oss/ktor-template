package test.ktortemplate.core.httphandler

import io.ktor.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.routing.get
import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.core.exception.AppException
import test.ktortemplate.core.exception.ErrorCode
import test.ktortemplate.core.service.CarService

internal class DefaultRoutesInjector : KoinComponent {
    val service: CarService by inject()
}

fun Route.defaultRoutes() {

    val injector = DefaultRoutesInjector()
    val service = injector.service

    get("/car/{id}") {
        val carId = call.parameters["id"]?.toLong() ?: -1

        when (val car = service.getCarById(carId)) {
            null -> call.respond(HttpStatusCode.NotFound)
            else -> call.respond(car)
        }
    }


    post("/car") {
        throw AppException(
            code = ErrorCode.NotImplemented,
            title = "Create car not implemented yet")
    }

}
