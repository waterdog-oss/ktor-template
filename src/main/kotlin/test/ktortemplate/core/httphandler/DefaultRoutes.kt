package test.ktortemplate.core.httphandler

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
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
        val newCar = call.receive<Car>()
        newCar.validate()

        val insertedCar = service.insertNewCar(CarSaveCommand(newCar.brand, newCar.model))
        call.respond(insertedCar)
    }
}
