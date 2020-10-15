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
     * Routes registered here will be the default ones if no Accept header is passed.
     */
    accept(contentType = ApiVersion.JSON.Latest.contentType) {
        get("/resource") {
            call.respond("{ \"response\": \"v2\" }")
        }
    }

    accept(contentType = ApiVersion.JSON.V1.contentType) {
        get("/resource") {
            call.respond("{ \"response\": \"v1\" }")
        }
    }

    accept(contentType = ApiVersion.XML.Latest.contentType) {
        get("/resource") {
            call.respond("<response>latest</response>")
        }
    }

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
