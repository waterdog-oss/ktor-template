package test.ktortemplate.core.httphandler

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be greater than`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.test.KoinTest
import org.koin.test.inject
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import test.ktortemplate.containers.PgSQLContainerFactory
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.persistance.CarRepository
import test.ktortemplate.core.testApp
import test.ktortemplate.core.utils.JsonSettings
import test.ktortemplate.core.utils.pagination.PageRequest
import test.ktortemplate.core.utils.pagination.PageResponse
import test.ktortemplate.core.utils.pagination.PaginationUtils
import java.util.UUID

@KtorExperimentalAPI
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestRoutes : KoinTest {

    companion object {
        @Container
        private val dbContainer = PgSQLContainerFactory.newInstance()
    }

    private val carRepository: CarRepository by inject()

    @AfterEach
    fun cleanDatabase() {
        val cars = carRepository.list(PageRequest(page = 0, size = Int.MAX_VALUE, sort = listOf(), filter = listOf()))
        cars.forEach {
            carRepository.delete(it.id)
        }
        carRepository.count() `should be equal to` 0
    }

    @Test
    fun `Fetching a car that does not exists returns a 404 Not Found`() = testAppWithConfig {
        with(handleRequest(HttpMethod.Get, "/cars/12345")) {
            response.status() `should be equal to` HttpStatusCode.NotFound
        }
    }

    @Test
    fun `Fetching a car that exists returns correctly`() = testAppWithConfig {
        val newCar = insertCar()

        with(handleRequest(HttpMethod.Get, "/cars/${newCar.id}")) {
            response.status() `should be equal to` HttpStatusCode.OK
            val car: Car = JsonSettings.mapper.readValue(response.content!!)
            car.id `should be equal to` newCar.id
            car.brand `should be equal to` newCar.brand
            car.model `should be equal to` newCar.model
        }
    }

    @Test
    fun `Creating a new car returns correctly`() = testAppWithConfig {
        val cmd = CarSaveCommand("brand", "model")

        with(
            handleRequest(HttpMethod.Post, "/cars") {
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

    @Nested
    @DisplayName("Test car listing: filtering and ordering")
    inner class TestCarList {

        @Test
        fun `The list endpoint works without any extra parameters`() = testAppWithConfig {
            val expectedCars = generateCars(5)
            with(handleRequest(HttpMethod.Get, "/cars")) {
                response.status() `should be equal to` HttpStatusCode.OK
                val res: PageResponse<Car> = JsonSettings.mapper.readValue(response.content!!)
                res.data.size `should be equal to` expectedCars.size
                res.data `should be equal to` expectedCars
            }
        }

        @Test
        fun `Test that the first page of the list has the appropriate parameters`() = testAppWithConfig {
            val expectedCars = generateCars(5)
            with(
                handleRequest(
                    HttpMethod.Get,
                    "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=3"
                )
            ) {
                response.status() `should be equal to` HttpStatusCode.OK
                val res: PageResponse<Car> = JsonSettings.mapper.readValue(response.content!!)
                res.data.size `should be equal to` 3
                res.data `should be equal to` expectedCars.subList(0, 3)

                // Verify pagination info
                res.meta.page `should be equal to` 0
                res.meta.size `should be equal to` 3
                res.meta.totalElements `should be equal to` expectedCars.size
                res.meta.totalPages `should be equal to` 2
                res.meta.first `should be equal to` true
                res.meta.last `should be equal to` false
                res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=3"
                res.links.first `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=3"
                res.links.prev `should be equal to` null
                res.links.next `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=1&${PaginationUtils.PAGE_SIZE}=3"
                res.links.last `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=1&${PaginationUtils.PAGE_SIZE}=3"
            }
        }

        @Test
        fun `Test that the second page of the list has the appropriate parameters`() = testAppWithConfig {
            val expectedCars = generateCars(5)
            with(
                handleRequest(
                    HttpMethod.Get,
                    "/cars?${PaginationUtils.PAGE_NUMBER}=1&${PaginationUtils.PAGE_SIZE}=3"
                )
            ) {
                response.status() `should be equal to` HttpStatusCode.OK
                val res: PageResponse<Car> = JsonSettings.mapper.readValue(response.content!!)
                res.data.size `should be equal to` 2
                res.data `should be equal to` expectedCars.subList(3, 5)

                // Verify pagination info
                res.meta.page `should be equal to` 1
                res.meta.size `should be equal to` 3
                res.meta.totalElements `should be equal to` expectedCars.size
                res.meta.totalPages `should be equal to` 2
                res.meta.first `should be equal to` false
                res.meta.last `should be equal to` true
                res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=1&${PaginationUtils.PAGE_SIZE}=3"
                res.links.first `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=3"
                res.links.prev `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=3"
                res.links.next `should be equal to` null
                res.links.last `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=1&${PaginationUtils.PAGE_SIZE}=3"
            }
        }

        @Test
        fun `Requesting a non-existing page is  handled gracefully`() = testAppWithConfig {
            generateCars(5)
            with(
                handleRequest(
                    HttpMethod.Get,
                    "/cars?${PaginationUtils.PAGE_NUMBER}=2&${PaginationUtils.PAGE_SIZE}=5"
                )
            ) {
                response.status() `should be equal to` HttpStatusCode.OK
                val res: PageResponse<Car> = JsonSettings.mapper.readValue(response.content!!)
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
        }

        @Test
        fun `Results are correctly sorted in ascending order`() = testAppWithConfig {
            val expectedCars = generateCars(5)
            with(
                handleRequest(
                    HttpMethod.Get,
                    "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_SORT}[id]=asc"
                )
            ) {
                response.status() `should be equal to` HttpStatusCode.OK
                val res: PageResponse<Car> = JsonSettings.mapper.readValue(response.content!!)
                res.data `should be equal to` expectedCars.sortedBy { it.id }

                // Verify pagination info
                res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_SORT}[id]=asc"
            }
        }

        @Test
        fun `Results are correctly sorted in descending order`() = testAppWithConfig {
            val expectedCars = generateCars(5)
            with(
                handleRequest(
                    HttpMethod.Get,
                    "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_SORT}[id]=desc"
                )
            ) {
                response.status() `should be equal to` HttpStatusCode.OK
                val res: PageResponse<Car> = JsonSettings.mapper.readValue(response.content!!)
                res.data `should be equal to` expectedCars.sortedByDescending { it.id }

                // Verify pagination info
                res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_SORT}[id]=desc"
            }
        }

        @Test
        fun `Results are correctly sorted by multiple parameters`() = testAppWithConfig {
            val expectedCars = generateCars(5)
            with(
                handleRequest(
                    HttpMethod.Get,
                    "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_SORT}[brand]=asc&${PaginationUtils.PAGE_SORT}[model]=desc"
                )
            ) {
                response.status() `should be equal to` HttpStatusCode.OK
                val res: PageResponse<Car> = JsonSettings.mapper.readValue(response.content!!)
                res.data `should be equal to` expectedCars.sortedWith(compareBy<Car> { it.brand }.thenByDescending { it.model })

                // Verify pagination info
                res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_SORT}[brand]=asc&${PaginationUtils.PAGE_SORT}[model]=desc"
            }
        }

        @Test
        fun `Results can be filtered`() = testAppWithConfig {
            val expectedCars = generateCars(5)
            val targetCar = expectedCars.random()
            with(
                handleRequest(
                    HttpMethod.Get,
                    "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_FILTER}[brand]=${targetCar.brand}&${PaginationUtils.PAGE_FILTER}[model]=${targetCar.model}"
                )
            ) {
                response.status() `should be equal to` HttpStatusCode.OK
                val res: PageResponse<Car> = JsonSettings.mapper.readValue(response.content!!)
                res.data `should be equal to` expectedCars.filter { it.brand == targetCar.brand && it.model == targetCar.model }

                // Verify pagination info
                res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_FILTER}[brand]=${targetCar.brand}&${PaginationUtils.PAGE_FILTER}[model]=${targetCar.model}"
            }
        }

        @Test
        fun `Individual filters may reference more than one value`() = testAppWithConfig {
            val expectedCars = generateCars(5)
            val ids = listOf(expectedCars.first().id, expectedCars.last().id)
            val queryParams = ids.joinToString(",")
            with(
                handleRequest(
                    HttpMethod.Get,
                    "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_FILTER}[id]=$queryParams"
                )
            ) {
                response.status() `should be equal to` HttpStatusCode.OK
                val res: PageResponse<Car> = JsonSettings.mapper.readValue(response.content!!)
                res.data `should be equal to` expectedCars.filter { it.id in ids }

                // Verify pagination info
                res.links.self `should be equal to` "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_FILTER}[id]=$queryParams"
            }
        }

        @Test
        fun `Results are empty when the filters do not match the data`() = testAppWithConfig {
            generateCars(5)
            with(
                handleRequest(
                    HttpMethod.Get,
                    "/cars?${PaginationUtils.PAGE_NUMBER}=0&${PaginationUtils.PAGE_SIZE}=10&${PaginationUtils.PAGE_FILTER}[brand]=brand000"
                )
            ) {
                response.status() `should be equal to` HttpStatusCode.OK
                val res: PageResponse<Car> = JsonSettings.mapper.readValue(response.content!!)
                res.data `should be equal to` listOf()
            }
        }
    }

    private fun generateCars(n: Int): List<Car> {
        val result: MutableList<Car> = mutableListOf()
        repeat(n) {
            val id = UUID.randomUUID()
            result.add(insertCar(brand = "BRAND: $id", model = "MODEL: $id"))
        }
        return result
    }

    private fun insertCar(
        brand: String = UUID.randomUUID().toString(),
        model: String = UUID.randomUUID().toString()
    ): Car {
        val newCar = CarSaveCommand(brand, model)
        return this.carRepository.save(newCar)
    }

    private fun <R> testAppWithConfig(test: TestApplicationEngine.() -> R) {
        testApp(dbContainer.configInfo(), test)
    }
}
