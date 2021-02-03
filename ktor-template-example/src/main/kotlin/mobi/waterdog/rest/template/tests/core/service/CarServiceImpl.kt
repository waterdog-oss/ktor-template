package mobi.waterdog.rest.template.tests.core.service

import mobi.waterdog.rest.template.database.DatabaseConnection
import mobi.waterdog.rest.template.exception.AppException
import mobi.waterdog.rest.template.exception.ErrorCodes
import mobi.waterdog.rest.template.pagination.PageRequest
import mobi.waterdog.rest.template.tests.core.model.Car
import mobi.waterdog.rest.template.tests.core.model.CarSaveCommand
import mobi.waterdog.rest.template.tests.core.model.Part
import mobi.waterdog.rest.template.tests.core.model.RegisterPartReplacementCommand
import mobi.waterdog.rest.template.tests.core.persistance.CarRepository
import mobi.waterdog.rest.template.tests.core.persistance.PartRepository
import org.slf4j.LoggerFactory

class CarServiceImpl(
    private val carRepository: CarRepository,
    private val partRepository: PartRepository,
    private val dbc: DatabaseConnection
) :
    CarService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun count(pageRequest: PageRequest): Int {
        return dbc.suspendedQuery {
            log.info("Counting cars from the repository")
            carRepository.count(pageRequest)
        }
    }

    override suspend fun exists(carId: Long): Boolean = dbc.suspendedQuery { carRepository.exists(carId) }

    override suspend fun getCarById(carId: Long): Car? {
        return dbc.suspendedQuery { carRepository.getById(carId) }
    }

    override suspend fun insertNewCar(newCar: CarSaveCommand): Car {
        return dbc.suspendedQuery { carRepository.save(newCar) }
    }

    override suspend fun updateCar(car: Car): Car {
        return dbc.suspendedQuery {
            if (!exists(car.id)) {
                throw AppException(ErrorCodes.NotFound, "Could not find car with id '${car.id}'.")
            }
            carRepository.update(car)
        }
    }

    override suspend fun registerPartReplacement(replacedParts: RegisterPartReplacementCommand): Car {
        // this runs the operation as a single transaction
        return dbc.suspendedQuery {
            val car = carRepository.getById(replacedParts.carId)
            requireNotNull(car) { "Car must exist" }
            for (part: Part in replacedParts.parts) {
                partRepository.addPartToCar(car.id, part)
            }

            car
        }
    }

    override suspend fun list(pageRequest: PageRequest): List<Car> {
        return dbc.suspendedQuery {
            carRepository.list(pageRequest)
        }
    }
}
