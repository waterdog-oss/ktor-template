package test.ktortemplate.core.service

import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.model.RegisterPartReplacementCommand
import test.ktortemplate.core.utils.pagination.PageRequest

interface CarService {
    suspend fun count(pageRequest: PageRequest): Int
    suspend fun getCarById(carId: Long): Car?
    suspend fun insertNewCar(newCar: CarSaveCommand): Car
    suspend fun registerPartReplacement(replacedParts: RegisterPartReplacementCommand): Car
    suspend fun list(pageRequest: PageRequest): List<Car>
}
