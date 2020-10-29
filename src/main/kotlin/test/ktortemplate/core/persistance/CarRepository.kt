package test.ktortemplate.core.persistance

import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.utils.pagination.PageRequest

interface CarRepository {
    fun exists(id: Long): Boolean
    fun save(car: CarSaveCommand): Car
    fun update(car: Car): Car
    fun getById(id: Long): Car?
    fun count(pageRequest: PageRequest = PageRequest()): Int
    fun delete(id: Long)
    fun list(pageRequest: PageRequest = PageRequest()): List<Car>
}
