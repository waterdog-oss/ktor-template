package test.ktortemplate.core.service

import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.model.PageRequest

interface CarService {
    fun count(pageRequest: PageRequest): Int
    fun getCarById(carId: Long): Car?
    fun insertNewCar(newCar: CarSaveCommand): Car
    fun list(pageRequest: PageRequest): List<Car>
}
