package test.ktortemplate.core.service

import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand

interface CarService {
    fun count(): Int
    fun getCarById(carId: Long): Car?
    fun insertNewCar(newCar: CarSaveCommand): Car
    fun list(): List<Car>
}