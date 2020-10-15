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
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.model.CarSaveCommandV1
import test.ktortemplate.core.service.CarService
import test.ktortemplate.core.utils.pagination.PageRequest
import test.ktortemplate.core.utils.pagination.PageResponse
import test.ktortemplate.core.utils.pagination.parsePageRequest
import test.ktortemplate.core.utils.versioning.ApiVersion
import test.ktortemplate.core.utils.versioning.acceptVersion

internal class DefaultRoutesInjector : KoinComponent {
    val carService: CarService by inject()
}

fun Route.defaultRoutes() {
    val injector = DefaultRoutesInjector()
    val carService = injector.carService

    /**
     * Routes that are not compatible with the latest API version.
     */
    acceptVersion(ApiVersion.Json.V1) {
        get("/cars") {
            val data = carService.list(PageRequest())
            call.respond(data)
        }

        post("/cars") {
            val request = call.receive<CarSaveCommandV1>()
            val newCar = carService.insertNewCar(CarSaveCommand(brand = request.brand, model = "defaultModel"))
            call.respond(newCar)
        }
    }

    /**
     * Default (if no accept is provided) and latest routes that are backward compatible with all versions.
     */
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
