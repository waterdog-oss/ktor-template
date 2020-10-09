package test.ktortemplate.core.persistance

import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.model.PageRequest

interface CarRepository {
    fun save(car: CarSaveCommand): Car
    fun getById(id: Long): Car?
    fun count(pageRequest: PageRequest = PageRequest.default()): Int
    fun delete(id: Long)
    fun list(pageRequest: PageRequest = PageRequest.default()): List<Car>
}
