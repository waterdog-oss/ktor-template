package test.ktortemplate.core.httphandler

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
import test.ktortemplate.core.utils.json.JsonSettings
import test.ktortemplate.core.utils.json.JsonSettings.PageResponseCarType
import test.ktortemplate.core.utils.json.fromJson
import test.ktortemplate.core.utils.pagination.PageRequest
import test.ktortemplate.core.utils.pagination.PageResponse
import test.ktortemplate.core.utils.pagination.PaginationUtils
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
        val cars = carRepository.list(PageRequest(page = 0, size = Int.MAX_VALUE, sort = listOf(), filter = listOf()))
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
        with(handleRequest(HttpMethod.Get, "/cars/12345")) {
            response.status() `should be equal to` HttpStatusCode.NotFound
        }
    }

    @Test
    fun `Fetching a car that exists returns correctly`() = testApp<Unit> {
        val newCar = insertCar()

        with(handleRequest(HttpMethod.Get, "/cars/${newCar.id}")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val car: Car = JsonSettings.mapper.fromJson(response.content)
            car.id `should be equal to` newCar.id
            car.brand `should be equal to` newCar.brand
            car.model `should be equal to` newCar.model
        }
    }

    @Test
    fun `Creating a new car returns correctly`() = testApp<Unit> {
        val cmd = CarSaveCommand("brand", "model")

        with(
            handleRequest(HttpMethod.Post, "/cars") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(JsonSettings.mapper.toJson(cmd))
            }
        ) {
            response.status() `should be equal to` HttpStatusCode.OK
            val car: Car = JsonSettings.mapper.fromJson(response.content)
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
        with(handleRequest(HttpMethod.Get, "/cars")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: PageResponse<Car> = JsonSettings.mapper.fromJson(response.content, PageResponseCarType)
            res.data.size `should be equal to` savedCars.size
            res.data `should be equal to` savedCars
        }

        // Get first page
        with(handleRequest(HttpMethod.Get, "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=3")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: PageResponse<Car> = JsonSettings.mapper.fromJson(response.content, PageResponseCarType)

            res.data.size `should be equal to` 3
            res.data `should be equal to` savedCars.subList(0, 3)

            // Verify pagination info
            res.meta.page `should be equal to` 0
            res.meta.size `should be equal to` 3
            res.meta.totalElements `should be equal to` savedCars.size
            res.meta.totalPages `should be equal to` 2
            res.meta.first `should be equal to` true
            res.meta.last `should be equal to` false
            res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=3"
            res.links.first `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=3"
            res.links.prev `should be equal to` null
            res.links.next `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=1&${PaginationUtils.PAGE_SIZE}=3"
            res.links.last `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=1&${PaginationUtils.PAGE_SIZE}=3"
        }

        // Get second page
        with(handleRequest(HttpMethod.Get, "/cars?${PaginationUtils.PAGE_NUMBER}=1&${PaginationUtils.PAGE_SIZE}=3")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: PageResponse<Car> = JsonSettings.mapper.fromJson(response.content, PageResponseCarType)
            res.data.size `should be equal to` 2
            res.data `should be equal to` savedCars.subList(3, 5)

            // Verify pagination info
            res.meta.page `should be equal to` 1
            res.meta.size `should be equal to` 3
            res.meta.totalElements `should be equal to` savedCars.size
            res.meta.totalPages `should be equal to` 2
            res.meta.first `should be equal to` false
            res.meta.last `should be equal to` true
            res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=1&${PaginationUtils.PAGE_SIZE}=3"
            res.links.first `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=3"
            res.links.prev `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=3"
            res.links.next `should be equal to` null
            res.links.last `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=1&${PaginationUtils.PAGE_SIZE}=3"
        }

        // Get not existing page
        with(handleRequest(HttpMethod.Get, "/cars?${PaginationUtils.PAGE_NUMBER}=2&${PaginationUtils.PAGE_SIZE}=5")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: PageResponse<Car> = JsonSettings.mapper.fromJson(response.content, PageResponseCarType)
            res.data.size `should be equal to` 0

            // Verify pagination info
            res.meta.first `should be equal to` false
            res.meta.last `should be equal to` false
            res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=2&${PaginationUtils.PAGE_SIZE}=5"
            res.links.first `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=5"
            res.links.prev `should be equal to` null
            res.links.next `should be equal to` null
            res.links.last `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=5"
        }

        // Get ascending order
        with(
            handleRequest(
                HttpMethod.Get,
                "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_SORT}[id]=asc"
            )
        ) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: PageResponse<Car> = JsonSettings.mapper.fromJson(response.content, PageResponseCarType)
            res.data `should be equal to` savedCars.sortedBy { it.id }

            // Verify pagination info
            res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_SORT}[id]=asc"
        }

        // Get descending order
        with(
            handleRequest(
                HttpMethod.Get,
                "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_SORT}[id]=desc"
            )
        ) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: PageResponse<Car> = JsonSettings.mapper.fromJson(response.content, PageResponseCarType)
            res.data `should be equal to` savedCars.sortedByDescending { it.id }

            // Verify pagination info
            res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_SORT}[id]=desc"
        }

        // Get multiple sorts
        with(
            handleRequest(
                HttpMethod.Get,
                "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_SORT}[brand]=asc&${PaginationUtils.PAGE_SORT}[model]=desc"
            )
        ) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: PageResponse<Car> = JsonSettings.mapper.fromJson(response.content, PageResponseCarType)
            res.data `should be equal to` savedCars.sortedWith(compareBy<Car> { it.brand }.thenByDescending { it.model })

            // Verify pagination info
            res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_SORT}[brand]=asc&${PaginationUtils.PAGE_SORT}[model]=desc"
        }

        // Get multiple filters
        with(
            handleRequest(
                HttpMethod.Get,
                "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_FILTER}[brand]=brand10&${PaginationUtils.PAGE_FILTER}[model]=model91"
            )
        ) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: PageResponse<Car> = JsonSettings.mapper.fromJson(response.content, PageResponseCarType)
            res.data `should be equal to` savedCars.filter { it.brand == "brand10" && it.model == "model91" }

            // Verify pagination info
            res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_FILTER}[brand]=brand10&${PaginationUtils.PAGE_FILTER}[model]=model91"
        }

        // No results found based on filters
        with(
            handleRequest(
                HttpMethod.Get,
                "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_FILTER}[brand]=brand000"
            )
        ) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: PageResponse<Car> = JsonSettings.mapper.fromJson(response.content, PageResponseCarType)
            res.data `should be equal to` listOf()
        }

        // Get multiple filters values
        with(
            handleRequest(
                HttpMethod.Get,
                "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_FILTER}[id]=1,2"
            )
        ) {
            response.status() `should be equal to` HttpStatusCode.OK
            val res: PageResponse<Car> = JsonSettings.mapper.fromJson(response.content, PageResponseCarType)
            res.data `should be equal to` savedCars.filter { it.id == 1L || it.id == 2L }

            // Verify pagination info
            res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_FILTER}[id]=1,2"
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
