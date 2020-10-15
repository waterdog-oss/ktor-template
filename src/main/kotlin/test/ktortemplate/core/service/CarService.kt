package test.ktortemplate.core.service

import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.model.RegisterPartReplacementCommand
import test.ktortemplate.core.utils.pagination.PageRequest

interface CarService {
    fun count(pageRequest: PageRequest): Int
    fun getCarById(carId: Long): Car?
    fun insertNewCar(newCar: CarSaveCommand): Car
    fun registerPartReplacement(replacedParts: RegisterPartReplacementCommand): Car
    fun list(pageRequest: PageRequest): List<Car>
}
