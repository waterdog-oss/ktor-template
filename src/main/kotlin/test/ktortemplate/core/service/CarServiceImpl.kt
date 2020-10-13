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

class CarServiceImpl : KoinComponent, CarService {

    private val carRepository: CarRepository by inject()
    private val partRepository: PartRepository by inject()
    private val dbc: DatabaseConnection by inject()

    override fun getCarById(carId: Long): Car? = this.carRepository.getById(carId)

    override fun insertNewCar(newCar: CarSaveCommand): Car = this.carRepository.save(newCar)

    override fun registerPartReplacement(replacedParts: RegisterPartReplacementCommand): Car {
        // this runs the operation as a single transaction
        return dbc.query {
            val car = carRepository.getById(replacedParts.carId)
            requireNotNull(car) { "Car must exist" }
            for (part: Part in replacedParts.parts) {
                partRepository.addPartToCar(car.id, part)
            }

            car
        }
    }
}
