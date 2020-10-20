package test.ktortemplate.core.persistance

import test.ktortemplate.core.model.Part

interface PartRepository {
    fun list(): List<Part>
    fun delete(partNo: Long)
    fun count(): Int
    fun getPartsForCar(carId: Long): List<Part>
    fun addPartToCar(carId: Long, part: Part): Part
}
