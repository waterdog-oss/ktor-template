package mobi.waterdog.rest.template.core.service

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import mobi.waterdog.rest.template.conf.EnvironmentConfigurator
import mobi.waterdog.rest.template.conf.database.DatabaseConnection
import mobi.waterdog.rest.template.containers.PgSQLContainerFactory
import mobi.waterdog.rest.template.core.model.Car
import mobi.waterdog.rest.template.core.model.CarSaveCommand
import mobi.waterdog.rest.template.core.model.Part
import mobi.waterdog.rest.template.core.model.RegisterPartReplacementCommand
import mobi.waterdog.rest.template.core.persistance.CarRepository
import mobi.waterdog.rest.template.core.persistance.PartRepository
import mobi.waterdog.rest.template.core.service.CarService

@Testcontainers
@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCarService : KoinTest {

    companion object {
        @Container
        private val dbContainer = PgSQLContainerFactory.newInstance()
    }

    private val carRepository: CarRepository by inject()
    private val partRepository: PartRepository by inject()
    private val carService: CarService by inject()
    private val dbc: DatabaseConnection by inject()

    @BeforeAll
    fun setup() {
        val appModules = EnvironmentConfigurator(dbContainer.configInfo()).getDependencyInjectionModules()
        startKoin { modules(appModules) }
    }

    @AfterEach
    fun cleanDatabase() {
        dbc.query {
            val cars = carRepository.list()
            cars.forEach {
                carRepository.delete(it.id)
            }
            carRepository.count() `should be equal to` 0

            val parts = partRepository.list()
            parts.forEach {
                partRepository.delete(it.partNo)
            }
            partRepository.count() `should be equal to` 0
        }
    }

    @AfterAll
    fun close() {
        stopKoin()
    }

    @Test
    fun `Parts can be added to a car`(): Unit = runBlocking {
        // Given: a car
        val car = createCar("Mercedes-Benz", "A 180")
        val oldPartsCount = countParts()

        // And: a set of parts one of which has a duplicate part number
        val partReplacement = RegisterPartReplacementCommand(
            carId = car.id,
            parts = listOf(
                Part(partNo = 1L, manufacturer = "Bosch", description = "Spark plug"),
                Part(partNo = 2L, manufacturer = "Wurth", description = "Air conditioner filter"),
            )
        )

        // When: a parts replacement is registered it fails
        carService.registerPartReplacement(partReplacement)

        // Expect: no parts have been associated with the car
        countParts() `should be equal to` oldPartsCount + partReplacement.parts.size
    }

    @Test
    fun `Test that nested transactions rollback as expected`(): Unit = runBlocking {
        // Given: a car
        val car = createCar("Opel", "Corsa")
        val oldPartsCount = countParts()

        // And: a set of parts one of which has a duplicate part number
        val partReplacement = RegisterPartReplacementCommand(
            carId = car.id,
            parts = listOf(
                Part(partNo = 1L, manufacturer = "Bosch", description = "Spark plug"),
                Part(partNo = 2L, manufacturer = "Wurth", description = "Air conditioner filter"),
                Part(partNo = 1L, manufacturer = "Bosch", description = "Spark plug") // note the duplicate part
            )
        )

        // When: a parts replacement is registered it fails
        assertThrows<Exception> {
            carService.registerPartReplacement(partReplacement)
        }

        // Expect: no parts have been associated with the car
        countParts() `should be equal to` oldPartsCount
    }

    private fun createCar(brand: String, model: String): Car = dbc.query {
        carRepository.save(CarSaveCommand(brand, model))
    }

    private fun countParts(): Int = dbc.query { partRepository.count() }
}
