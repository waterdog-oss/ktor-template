package test.ktortemplate.core.persistance

import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.utils.SortField

interface CarRepository {
    fun save(car: CarSaveCommand): Car
    fun getById(id: Long): Car?
    fun count(): Int
    fun delete(id: Long)
    fun list(limit: Int = 10, offset: Int = 0, sortFields: List<SortField> = listOf()): List<Car>
}
