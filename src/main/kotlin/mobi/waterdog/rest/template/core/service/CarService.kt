package mobi.waterdog.rest.template.core.service

import mobi.waterdog.rest.template.core.model.Car
import mobi.waterdog.rest.template.core.model.CarSaveCommand
import mobi.waterdog.rest.template.core.model.RegisterPartReplacementCommand
import mobi.waterdog.rest.template.core.utils.pagination.PageRequest

interface CarService {
    suspend fun exists(carId: Long): Boolean
    suspend fun count(pageRequest: PageRequest): Int
    suspend fun getCarById(carId: Long): Car?
    suspend fun insertNewCar(newCar: CarSaveCommand): Car
    suspend fun updateCar(car: Car): Car
    suspend fun registerPartReplacement(replacedParts: RegisterPartReplacementCommand): Car
    suspend fun list(pageRequest: PageRequest): List<Car>
}
