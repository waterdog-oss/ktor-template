package test.ktortemplate.core.persistance

import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand

interface CarRepository {
    fun save(car: CarSaveCommand): Car
    fun getById(id: Long): Car?
    fun count(): Int
    fun delete(id: Long)
    fun list(): List<Car>
}
