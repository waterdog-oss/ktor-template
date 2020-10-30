package mobi.waterdog.rest.template.core.persistance

import mobi.waterdog.rest.template.core.model.Car
import mobi.waterdog.rest.template.core.model.CarSaveCommand
import mobi.waterdog.rest.template.core.utils.pagination.PageRequest

interface CarRepository {
    fun exists(id: Long): Boolean
    fun save(car: CarSaveCommand): Car
    fun update(car: Car): Car
    fun getById(id: Long): Car?
    fun count(pageRequest: PageRequest = PageRequest()): Int
    fun delete(id: Long)
    fun list(pageRequest: PageRequest = PageRequest()): List<Car>
}
