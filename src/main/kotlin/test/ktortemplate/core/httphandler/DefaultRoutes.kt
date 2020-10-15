package test.ktortemplate.core.httphandler

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.accept
import io.ktor.routing.get
import io.ktor.routing.post
import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.model.CarSaveCommandV1
import test.ktortemplate.core.service.CarService
import test.ktortemplate.core.utils.pagination.PageResponse
import test.ktortemplate.core.utils.pagination.parsePageRequest
import test.ktortemplate.core.utils.versioning.ApiVersion

internal class DefaultRoutesInjector : KoinComponent {
    val carService: CarService by inject()
}

fun Route.defaultRoutes() {
    val injector = DefaultRoutesInjector()
    val carService = injector.carService

    /**
     * Routes for XML latest version.
     * Routes registered here will be the default ones if no Accept header is passed.
     */
    accept(contentType = ApiVersion.JSON.Latest.contentType) {
        get("/cars") {
            val pageRequest = call.parsePageRequest()
            val totalElements = carService.count(pageRequest)
            val data = carService.list(pageRequest)
            call.respond(
                PageResponse.from(
                    pageRequest = pageRequest,
                    totalElements = totalElements,
                    data = data,
                    path = call.request.path()
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

    /**
     * Routes for JSON v1
     */
    accept(contentType = ApiVersion.JSON.V1.contentType) {
        /**
         * Example where for a new field model introduzed in API v2 that woul
         */
        post("/cars") {
            val request = call.receive<CarSaveCommandV1>()
            val newCar = carService.insertNewCar(CarSaveCommand(brand = request.brand, model = "defaultModel"))
            call.respond(newCar)
        }
    }
}
