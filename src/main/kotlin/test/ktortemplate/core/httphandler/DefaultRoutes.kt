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

internal class DefaultRoutesInjector : KoinComponent {
    val service: CarService by inject()
}

fun Route.defaultRoutes() {

    val injector = DefaultRoutesInjector()
    val service = injector.service

    get("/car") {
        // TODO: Add suppiort for pagination and sort
        //val count = service.count()
        val list = service.list()
        call.respond(list)
    }

    get("/car/{id}") {
        val carId = call.parameters["id"]?.toLong() ?: -1

        when (val car = service.getCarById(carId)) {
            null -> call.respond(HttpStatusCode.NotFound)
            else -> call.respond(car)
        }
    }

    post("/car") {
        val newCar = service.insertNewCar(call.receive())
        call.respond(newCar)
    }
}
