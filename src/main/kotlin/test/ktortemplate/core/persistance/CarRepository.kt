package test.ktortemplate.core.persistance

import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.utils.pagination.PageRequest

interface CarRepository {
    suspend fun save(car: CarSaveCommand): Car
    suspend fun getById(id: Long): Car?
    suspend fun count(pageRequest: PageRequest = PageRequest()): Int
    suspend fun delete(id: Long)
    suspend fun list(pageRequest: PageRequest = PageRequest()): List<Car>
}
