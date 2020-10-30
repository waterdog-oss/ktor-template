package mobi.waterdog.rest.template.core.persistance.sql

import mobi.waterdog.rest.template.core.model.Part
import mobi.waterdog.rest.template.core.persistance.PartRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

internal class PartRepositoryImpl : PartRepository {

    override fun list(): List<Part> {
        return PartMappingsTable.selectAll().map { toModel(it) }
    }

    override fun delete(partNo: Long) {
        PartMappingsTable.deleteWhere { PartMappingsTable.partNo eq partNo }
    }

    override fun count(): Int {
        return PartMappingsTable.selectAll().count().toInt()
    }

    override fun getPartsForCar(carId: Long): List<Part> {
        return PartMappingsTable.select { PartMappingsTable.carId eq carId }.map { toModel(it) }
    }

    override fun addPartToCar(carId: Long, part: Part): Part {
        PartMappingsTable.insert {
            it[PartMappingsTable.carId] = carId
            it[partNo] = part.partNo
            it[description] = part.description
            it[manufacturer] = part.description
        }
        return part
    }

    private fun toModel(row: ResultRow): Part {
        return Part(
            partNo = row[PartMappingsTable.partNo],
            manufacturer = row[PartMappingsTable.manufacturer],
            description = row[PartMappingsTable.description]
        )
    }
}
