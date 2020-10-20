package test.ktortemplate.core.persistance

import test.ktortemplate.core.model.Part

interface PartRepository {
    suspend fun list(): List<Part>
    suspend fun delete(partNo: Long)
    suspend fun count(): Int
    suspend fun getPartsForCar(carId: Long): List<Part>
    suspend fun addPartToCar(carId: Long, part: Part): Part
}
