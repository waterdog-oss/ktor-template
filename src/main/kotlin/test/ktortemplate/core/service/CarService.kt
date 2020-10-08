package test.ktortemplate.core.service

import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.utils.SortField

interface CarService {
    fun count(): Int
    fun getCarById(carId: Long): Car?
    fun insertNewCar(newCar: CarSaveCommand): Car
    fun list(limit: Int, offset: Int, sortFields: List<SortField>): List<Car>
}
