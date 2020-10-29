package test.ktortemplate.core.httphandler

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.core.exception.AppException
import test.ktortemplate.core.exception.ErrorCode
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.service.CarService
import test.ktortemplate.core.service.PersonService
import test.ktortemplate.core.utils.pagination.PageResponse
import test.ktortemplate.core.utils.pagination.parsePageRequest
import test.ktortemplate.core.utils.versioning.ApiVersion

internal class DefaultRoutesInjector : KoinComponent {
    val carService: CarService by inject()
    val personService: PersonService by inject()
}

fun Route.defaultRoutes() {
    val apiVersion = ApiVersion.Latest
    val injector = DefaultRoutesInjector()
    val carService = injector.carService
    val personService = injector.personService

    get("/$apiVersion/cars") {
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

    get("/$apiVersion/cars/{id}") {
        val carId = call.parameters["id"]?.toLong() ?: -1
        when (val car = carService.getCarById(carId)) {
            null -> call.respond(HttpStatusCode.NotFound)
            else -> call.respond(car)
        }
    }

    post("/$apiVersion/cars") {
        val newCar = call.receive<Car>()
        newCar.validate()

        val insertedCar = carService.insertNewCar(CarSaveCommand(newCar.brand, newCar.model))
        call.respond(insertedCar)
    }

    put("/cars/{id}") {
        val car = call.receive<Car>()
        car.validate()

        val carId = call.parameters["id"]?.toLong() ?: -1
        if (carId != car.id) throw AppException(ErrorCode.InvalidUserInput, "Received ids doesn't match.")

        val updatedCar = carService.updateCar(car)
        call.respond(updatedCar)
    }

    get("/persons") {
        val pageRequest = call.parsePageRequest()
        val totalElements = personService.count(pageRequest)
        val data = personService.list(pageRequest)
        call.respond(
            PageResponse.from(
                pageRequest = pageRequest,
                totalElements = totalElements,
                data = data,
                path = call.request.path()
            )
        )
    }
}
