package test.ktortemplate.core.service

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
import test.ktortemplate.core.initDbCore
import test.ktortemplate.core.initServicesAndRepos
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.model.Part
import test.ktortemplate.core.model.RegisterPartReplacementCommand
import test.ktortemplate.core.persistance.CarRepository
import test.ktortemplate.core.persistance.PartRepository

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCarService : KoinTest {

    private val carRepository: CarRepository by inject()
    private val partRepository: PartRepository by inject()
    private val carService: CarService by inject()

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

        val parts = partRepository.list()
        parts.forEach {
            partRepository.delete(it.partNo)
        }
        partRepository.count() `should be equal to` 0
    }

    @AfterAll
    fun close() {
        stopKoin()
    }

    @Test
    fun `Parts can be added to a car`() {
        // Given: a car
        val car = carRepository.save(CarSaveCommand("Mercedes-Benz", "A 180"))
        val oldPartsCount = partRepository.count()

        // And: a set of parts one of which has a duplicate part number
        val partReplacement = RegisterPartReplacementCommand(
            carId = car.id,
            parts = listOf(
                Part(partNo = 1L, manufacturer = "Bosch", desc = "Spark plug"),
                Part(partNo = 2L, manufacturer = "Wurth", desc = "Air conditioner filter"),
            )
        )

        // When: a parts replacement is registered it fails
        carService.registerPartReplacement(partReplacement)

        // Expect: no parts have been associated with the car
        partRepository.count() `should be equal to` oldPartsCount + partReplacement.parts.size
    }

    @Test
    fun `Test that nested transactions rollback as expected`() {
        // Given: a car
        val car = carRepository.save(CarSaveCommand("Mercedes-Benz", "A 180"))
        val oldPartsCount = partRepository.count()

        // And: a set of parts one of which has a duplicate part number
        val partReplacement = RegisterPartReplacementCommand(
            carId = car.id,
            parts = listOf(
                Part(partNo = 1L, manufacturer = "Bosch", desc = "Spark plug"),
                Part(partNo = 2L, manufacturer = "Wurth", desc = "Air conditioner filter"),
                Part(partNo = 1L, manufacturer = "Bosch", desc = "Spark plug") // note the duplicate part
            )
        )

        // When: a parts replacement is registered it fails
        assertThrows<Exception> {
            carService.registerPartReplacement(partReplacement)
        }

        // Expect: no parts have been associated with the car
        partRepository.count() `should be equal to` oldPartsCount
    }
}
