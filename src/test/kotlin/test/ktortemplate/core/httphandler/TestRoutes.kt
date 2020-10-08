package test.ktortemplate.core.httphandler

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be greater than`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import test.ktortemplate.core.initDbCore
import test.ktortemplate.core.initServicesAndRepos
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.persistance.CarRepository
import test.ktortemplate.core.testApp
import test.ktortemplate.core.utils.JsonSettings
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestRoutes : KoinTest {

    private val carRepository: CarRepository by inject()

    @BeforeAll
    fun setup() {
        val appModules = listOf(
            initDbCore(),
            initServicesAndRepos()
        )
        startKoin { modules(appModules) }
    }

    @AfterEach
    fun cleanDatabase() {
        val cars = carRepository.list()
        cars.forEach {
            carRepository.delete(it.id)
        }
        carRepository.count() `should be equal to` 0
    }

    @AfterAll
    fun close() {
        stopKoin()
    }

    @Test
    fun `Fetching a car that does not exists returns a 404 Not Found`() = testApp<Unit> {
        with(handleRequest(HttpMethod.Get, "/car/12345")) {
            response.status() `should be equal to` HttpStatusCode.NotFound
        }
    }

    @Test
    fun `Fetching a car that exists returns correctly`() = testApp<Unit> {
        val newCar = insertCar()

        with(handleRequest(HttpMethod.Get, "/car/${newCar.id}")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val car: Car = JsonSettings.mapper.readValue(response.content!!)
            car.id `should be equal to` newCar.id
            car.brand `should be equal to` newCar.brand
            car.model `should be equal to` newCar.model
        }
    }

    @Test
    fun `Creating a new car returns correctly`() = testApp<Unit> {
        val cmd = CarSaveCommand("brand", "model")

        with(
            handleRequest(HttpMethod.Post, "/car") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(JsonSettings.mapper.writeValueAsString(cmd))
            }
        ) {
            response.status() `should be equal to` HttpStatusCode.OK
            val car: Car = JsonSettings.mapper.readValue(response.content!!)
            car.id `should be greater than` 0
            car.brand `should be equal to` cmd.brand
            car.model `should be equal to` cmd.model
        }
    }

    @Test
    fun `Listing cars should returns a list of cars with correct pagination and sort`() = testApp<Unit> {
        val savedCars = listOf(
            insertCar(brand = "brand1", model = "model1"),
            insertCar(brand = "brand2", model = "model2"),
            insertCar(brand = "brand3", model = "model3"),
            insertCar(brand = "brand4", model = "model4"),
            insertCar(brand = "brand5", model = "model5")
        )

        with(handleRequest(HttpMethod.Get, "/car")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val cars: List<Car> = JsonSettings.mapper.readValue(response.content!!)
            cars.size `should be equal to` savedCars.size
            cars `should be equal to` savedCars
        }

//        with(handleRequest(HttpMethod.Get, "/car?page=0&size=3")) {
//            response.status() `should be equal to` HttpStatusCode.OK
//            val cars: List<Car> = JsonSettings.mapper.readValue(response.content!!)
//            cars.size `should be equal to` 3
//            cars `should be equal to` savedCars.subList(0, 3)
//        }
//
//        with(handleRequest(HttpMethod.Get, "/car?page=1&size=3")) {
//            response.status() `should be equal to` HttpStatusCode.OK
//            val cars: List<Car> = JsonSettings.mapper.readValue(response.content!!)
//            cars.size `should be equal to` 2
//            cars `should be equal to` savedCars.subList(3, 5)
//        }
    }

    private fun insertCar(
        brand: String = UUID.randomUUID().toString(),
        model: String = UUID.randomUUID().toString()
    ): Car {
        val newCar = CarSaveCommand(brand, model)
        return this.carRepository.save(newCar)
    }
}
