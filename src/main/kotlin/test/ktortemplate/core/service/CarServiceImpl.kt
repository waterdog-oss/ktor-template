package test.ktortemplate.core.service

import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.persistance.CarRepository
import test.ktortemplate.core.utils.SortField

class CarServiceImpl : KoinComponent, CarService {

    private val carRepository: CarRepository by inject()

    override fun count(): Int = this.carRepository.count()

    override fun getCarById(carId: Long): Car? = this.carRepository.getById(carId)

    override fun insertNewCar(newCar: CarSaveCommand): Car = this.carRepository.save(newCar)

    override fun list(limit: Int, offset: Int, sortFields: List<SortField>): List<Car> =
        this.carRepository.list(limit, offset, sortFields)
}
