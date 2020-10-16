package test.ktortemplate.core.persistance.sql

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.conf.database.DatabaseConnection
import test.ktortemplate.core.model.Part
import test.ktortemplate.core.persistance.PartRepository

internal class PartRepositoryImpl : PartRepository, KoinComponent {

    private val dbc: DatabaseConnection by inject()

    override fun list(): List<Part> {
        return dbc.query { PartMappingsTable.selectAll().map { toModel(it) } }
    }

    override fun delete(partNo: Long) {
        dbc.query { PartMappingsTable.deleteWhere { PartMappingsTable.partNo eq partNo } }
    }

    override fun count(): Int {
        return dbc.query { PartMappingsTable.selectAll().count().toInt() }
    }

    override fun getPartsForCar(carId: Long): List<Part> {
        return dbc.query { PartMappingsTable.select { PartMappingsTable.carId eq carId }.map { toModel(it) } }
    }

    override fun addPartToCar(carId: Long, part: Part): Part {
        dbc.query {
            PartMappingsTable.insert {
                it[PartMappingsTable.carId] = carId
                it[partNo] = part.partNo
                it[description] = part.description
                it[manufacturer] = part.description
            }
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
