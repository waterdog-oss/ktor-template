package mobi.waterdog.rest.template.tests.core.persistance

import mobi.waterdog.rest.template.tests.core.model.Part

interface PartRepository {
    fun list(): List<Part>
    fun delete(partNo: Long)
    fun count(): Int
    fun getPartsForCar(carId: Long): List<Part>
    fun addPartToCar(carId: Long, part: Part): Part
}
