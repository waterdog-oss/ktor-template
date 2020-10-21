package test.ktortemplate.core.service

import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.conf.database.DatabaseConnection
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.model.Part
import test.ktortemplate.core.model.RegisterPartReplacementCommand
import test.ktortemplate.core.persistance.CarRepository
import test.ktortemplate.core.persistance.PartRepository
import test.ktortemplate.core.utils.pagination.PageRequest

class CarServiceImpl : KoinComponent, CarService {

    private val carRepository: CarRepository by inject()
    private val partRepository: PartRepository by inject()
    private val dbc: DatabaseConnection by inject()

    override suspend fun count(pageRequest: PageRequest): Int {
        return dbc.suspendedQuery { carRepository.count(pageRequest) }
    }

    override suspend fun getCarById(carId: Long): Car? {
        return dbc.suspendedQuery { carRepository.getById(carId) }
    }

    override suspend fun insertNewCar(newCar: CarSaveCommand): Car {
        return dbc.suspendedQuery { carRepository.save(newCar) }
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
