package mobi.waterdog.rest.template.tests.core.httphandler

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import mobi.waterdog.rest.template.pagination.PageResponse
import mobi.waterdog.rest.template.pagination.parsePageRequest
import mobi.waterdog.rest.template.tests.core.model.Car
import mobi.waterdog.rest.template.tests.core.model.CarSaveCommand
import mobi.waterdog.rest.template.tests.core.service.CarService
import mobi.waterdog.rest.template.tests.core.service.PersonService
import mobi.waterdog.rest.template.tests.core.utils.pagination.respondPaged
import mobi.waterdog.rest.template.tests.core.utils.versioning.ApiVersion
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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
        call.respondPaged(
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
        val newCar = call.receive<CarSaveCommand>()
        newCar.validate()

        val insertedCar = carService.insertNewCar(CarSaveCommand(newCar.brand, newCar.model))
        call.respond(insertedCar)
    }

    put("/$apiVersion/cars/{id}") {
        val car = call.receive<CarSaveCommand>()
        car.validate()
        val carId = call.parameters["id"]?.toLong() ?: -1
        val carToUpdate = Car(carId, car.brand, car.model, car.wheels)

        val updatedCar = carService.updateCar(carToUpdate)
        call.respond(updatedCar)
    }

    get("/$apiVersion/persons") {
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
