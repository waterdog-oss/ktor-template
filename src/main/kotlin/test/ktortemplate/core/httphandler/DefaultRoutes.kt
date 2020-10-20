package test.ktortemplate.core.httphandler

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
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
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.service.CarService
import test.ktortemplate.core.utils.json.JsonSettings
import test.ktortemplate.core.utils.pagination.PageResponse
import test.ktortemplate.core.utils.pagination.parsePageRequest
import java.lang.reflect.Type

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

        val type: Type = Types.newParameterizedType(PageResponse::class.java, Car::class.java)
        val jsonAdapter: JsonAdapter<PageResponse<Car>> = JsonSettings.mapper.adapter(type)

        call.respond(
            JsonSettings.toJson(
                PageResponse.from(
                    pageRequest = pageRequest,
                    totalElements = totalElements,
                    data = data,
                    path = call.request.path()
                ),
                jsonAdapter
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
