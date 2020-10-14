package test.ktortemplate.core.httphandler

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import java.util.UUID
import org.amshove.kluent.`should equal`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import test.ktortemplate.core.fromJson
import test.ktortemplate.core.initDbCore
import test.ktortemplate.core.initServicesAndRepos
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.persistance.CarRepository
import test.ktortemplate.core.testApp
import test.ktortemplate.core.utils.JsonSettings

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
        carRepository.count() `should equal` 0
    }

    @AfterAll
    fun close() {
        stopKoin()
    }

    @Test
    fun `Fetching a car that does not exists returns a 404 Not Found`() = testApp<Unit> {
        with(handleRequest(HttpMethod.Get, "/car/12345")) {
            response.status() `should equal` HttpStatusCode.NotFound
        }
    }

    @Test
    fun `Fetching a car that exists returns correctly`() = testApp<Unit> {
        val newCar = insertCar()

        with(handleRequest(HttpMethod.Get, "/car/${newCar.id}")) {
            response.status() `should equal` HttpStatusCode.OK
            val car: Car = JsonSettings.mapper.fromJson(response.content)

            car.id `should equal` newCar.id
            car.brand `should equal` newCar.brand
            car.model `should equal` newCar.model
        }
    }

    private fun insertCar(
        brand: String = UUID.randomUUID().toString(),
        model: String = UUID.randomUUID().toString()
    ): Car {
        val newCar = CarSaveCommand(brand, model)
        return this.carRepository.save(newCar)
    }
}
