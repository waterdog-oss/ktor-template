package test.ktortemplate.core.httphandler

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.core.service.CarService
import test.ktortemplate.core.utils.generatePaginationHeaders
import test.ktortemplate.core.utils.parsePageRequest

internal class DefaultRoutesInjector : KoinComponent {
    val carService: CarService by inject()
}

fun Route.defaultRoutes() {
    val injector = DefaultRoutesInjector()
    val carService = injector.carService

    get("/cars") {
        val pageRequest = call.parsePageRequest()
        val count = carService.count(pageRequest)
        val list = carService.list(pageRequest)
        call.generatePaginationHeaders(pageRequest = pageRequest, totalElements = count)
        call.respond(list)
    }

    get("/cars/{id}") {
        val carId = call.parameters["id"]?.toLong() ?: -1

        when (val car = carService.getCarById(carId)) {
            null -> call.respond(HttpStatusCode.NotFound)
            else -> call.respond(car)
        }
    }

    post("/cars") {
        val newCar = carService.insertNewCar(call.receive())
        call.respond(newCar)
    }
}
