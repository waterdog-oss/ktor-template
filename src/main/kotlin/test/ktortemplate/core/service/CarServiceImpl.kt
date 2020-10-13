package test.ktortemplate.core.service

import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.persistance.CarRepository
import test.ktortemplate.core.utils.pagination.PageRequest

class CarServiceImpl : KoinComponent, CarService {

    private val carRepository: CarRepository by inject()

    override fun count(pageRequest: PageRequest): Int = this.carRepository.count(pageRequest)

    override fun getCarById(carId: Long): Car? = this.carRepository.getById(carId)

    override fun insertNewCar(newCar: CarSaveCommand): Car = this.carRepository.save(newCar)

    override fun list(pageRequest: PageRequest): List<Car> =
        this.carRepository.list(pageRequest)
}
