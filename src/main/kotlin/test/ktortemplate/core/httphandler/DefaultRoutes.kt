package test.ktortemplate.core.httphandler

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.core.service.CarService
import test.ktortemplate.core.utils.json.JsonSettings
import test.ktortemplate.core.utils.pagination.PageResponse
import test.ktortemplate.core.utils.pagination.parsePageRequest

internal class DefaultRoutesInjector : KoinComponent {
    val carService: CarService by inject()
}

fun Route.defaultRoutes() {
    val injector = DefaultRoutesInjector()
    val carService = injector.carService

    get("/cars") {
        val pageRequest = call.parsePageRequest()
        val totalElements = carService.count(pageRequest)
        val data = carService.list(pageRequest)
        call.respond(
            JsonSettings.toJson(
                PageResponse.from(
                    pageRequest = pageRequest,
                    totalElements = totalElements,
                    data = data,
                    path = call.request.path()
                )
            )
        )
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
