package test.ktortemplate.core.httphandler

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import net.logstash.logback.argument.StructuredArguments.kv
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.service.CarService
import test.ktortemplate.core.utils.pagination.PageResponse
import test.ktortemplate.core.utils.pagination.parsePageRequest

internal class DefaultRoutesInjector : KoinComponent {
    val carService: CarService by inject()
}

fun Route.defaultRoutes() {
    val log = LoggerFactory.getLogger(this::class.java)
    val injector = DefaultRoutesInjector()
    val carService = injector.carService

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
        log.info("Received request")
        val carId = call.parameters["id"]?.toLong() ?: -1
        when (val car = carService.getCarById(carId)) {
            null -> call.respond(HttpStatusCode.NotFound)
            else -> {
                log.info("Returning response", kv("car", car)) // adds car as json to logging
                call.respond(car)
            }
        }
    }

    post("/cars") {
        val newCar = call.receive<Car>()
        newCar.validate()

        val insertedCar = carService.insertNewCar(CarSaveCommand(newCar.brand, newCar.model))
        call.respond(insertedCar)
    }
}
