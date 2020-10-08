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
import test.ktortemplate.core.utils.PaginationHeader
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
        val cars = carRepository.list(limit = Int.MAX_VALUE)
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
            carRepository.count() `should be equal to` 1
        }
    }

    @Test
    fun `Listing cars should returns a list of cars with correct pagination and sort`() = testApp<Unit> {
        val savedCars = listOf(
            insertCar(brand = "brand10", model = "model91"),
            insertCar(brand = "brand72", model = "model12"),
            insertCar(brand = "brand43", model = "model33"),
            insertCar(brand = "brand34", model = "model24"),
            insertCar(brand = "brand34", model = "model95")
        )

        // Get all
        with(handleRequest(HttpMethod.Get, "/car")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val cars: List<Car> = JsonSettings.mapper.readValue(response.content!!)
            cars.size `should be equal to` savedCars.size
            cars `should be equal to` savedCars
        }

        // Get first page
        with(handleRequest(HttpMethod.Get, "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=3")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val cars: List<Car> = JsonSettings.mapper.readValue(response.content!!)
            cars.size `should be equal to` 3
            cars `should be equal to` savedCars.subList(0, 3)

            // Verify pagination headers
            response.headers[PaginationHeader.HEADER_PAGE] `should be equal to` "0"
            response.headers[PaginationHeader.HEADER_SIZE] `should be equal to` "3"
            response.headers[PaginationHeader.HEADER_TOTAL_ELEMENTS] `should be equal to` savedCars.size.toString()
            response.headers[PaginationHeader.HEADER_TOTAL_PAGES] `should be equal to` "2"
            response.headers[PaginationHeader.HEADER_FIRST] `should be equal to` "true"
            response.headers[PaginationHeader.HEADER_LAST] `should be equal to` "false"
            response.headers[PaginationHeader.HEADER_LINKS_SELF] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=3"
            response.headers[PaginationHeader.HEADER_LINKS_FIRST] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=3"
            response.headers[PaginationHeader.HEADER_LINKS_PREV] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=3"
            response.headers[PaginationHeader.HEADER_LINKS_NEXT] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=1&${PaginationHeader.PAGE_SIZE}=3"
            response.headers[PaginationHeader.HEADER_LINKS_LAST] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=1&${PaginationHeader.PAGE_SIZE}=3"
        }

        // Get second page
        with(handleRequest(HttpMethod.Get, "/car?${PaginationHeader.PAGE_NUMBER}=1&${PaginationHeader.PAGE_SIZE}=3")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val cars: List<Car> = JsonSettings.mapper.readValue(response.content!!)
            cars.size `should be equal to` 2
            cars `should be equal to` savedCars.subList(3, 5)

            // Verify pagination headers
            response.headers[PaginationHeader.HEADER_PAGE] `should be equal to` "1"
            response.headers[PaginationHeader.HEADER_SIZE] `should be equal to` "3"
            response.headers[PaginationHeader.HEADER_TOTAL_ELEMENTS] `should be equal to` savedCars.size.toString()
            response.headers[PaginationHeader.HEADER_TOTAL_PAGES] `should be equal to` "2"
            response.headers[PaginationHeader.HEADER_FIRST] `should be equal to` "false"
            response.headers[PaginationHeader.HEADER_LAST] `should be equal to` "true"
            response.headers[PaginationHeader.HEADER_LINKS_SELF] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=1&${PaginationHeader.PAGE_SIZE}=3"
            response.headers[PaginationHeader.HEADER_LINKS_FIRST] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=3"
            response.headers[PaginationHeader.HEADER_LINKS_PREV] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=3"
            response.headers[PaginationHeader.HEADER_LINKS_NEXT] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=1&${PaginationHeader.PAGE_SIZE}=3"
            response.headers[PaginationHeader.HEADER_LINKS_LAST] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=1&${PaginationHeader.PAGE_SIZE}=3"
        }

        // Get not existing page
        with(handleRequest(HttpMethod.Get, "/car?${PaginationHeader.PAGE_NUMBER}=2&${PaginationHeader.PAGE_SIZE}=3")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val cars: List<Car> = JsonSettings.mapper.readValue(response.content!!)
            cars.size `should be equal to` 0

            // Verify pagination headers
            response.headers[PaginationHeader.HEADER_FIRST] `should be equal to` "false"
            response.headers[PaginationHeader.HEADER_LAST] `should be equal to` "false"
            response.headers[PaginationHeader.HEADER_LINKS_SELF] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=2&${PaginationHeader.PAGE_SIZE}=3"
            response.headers[PaginationHeader.HEADER_LINKS_FIRST] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=3"
            response.headers[PaginationHeader.HEADER_LINKS_PREV] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=1&${PaginationHeader.PAGE_SIZE}=3"
            response.headers[PaginationHeader.HEADER_LINKS_NEXT] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=1&${PaginationHeader.PAGE_SIZE}=3"
            response.headers[PaginationHeader.HEADER_LINKS_LAST] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=1&${PaginationHeader.PAGE_SIZE}=3"
        }

        // Get ascending order
        with(
            handleRequest(
                HttpMethod.Get,
                "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=10&${PaginationHeader.PAGE_SORT}=id"
            )
        ) {
            response.status() `should be equal to` HttpStatusCode.OK
            val cars: List<Car> = JsonSettings.mapper.readValue(response.content!!)
            cars `should be equal to` savedCars.sortedBy { it.id }

            // Verify pagination headers
            response.headers[PaginationHeader.HEADER_LINKS_SELF] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=10&${PaginationHeader.PAGE_SORT}=id"
        }

        // Get descending order
        with(
            handleRequest(
                HttpMethod.Get,
                "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=10&${PaginationHeader.PAGE_SORT}=-id"
            )
        ) {
            response.status() `should be equal to` HttpStatusCode.OK
            val cars: List<Car> = JsonSettings.mapper.readValue(response.content!!)
            cars `should be equal to` savedCars.sortedByDescending { it.id }

            // Verify pagination headers
            response.headers[PaginationHeader.HEADER_LINKS_SELF] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=10&${PaginationHeader.PAGE_SORT}=-id"
        }

        // Get multiple sorts
        with(
            handleRequest(
                HttpMethod.Get,
                "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=10&${PaginationHeader.PAGE_SORT}=brand,-model"
            )
        ) {
            response.status() `should be equal to` HttpStatusCode.OK
            val cars: List<Car> = JsonSettings.mapper.readValue(response.content!!)
            cars `should be equal to` savedCars.sortedWith(compareBy<Car> { it.brand }.thenByDescending { it.model })

            // Verify pagination headers
            response.headers[PaginationHeader.HEADER_LINKS_SELF] `should be equal to` "/car?${PaginationHeader.PAGE_NUMBER}=0&${PaginationHeader.PAGE_SIZE}=10&${PaginationHeader.PAGE_SORT}=brand,-model"
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
