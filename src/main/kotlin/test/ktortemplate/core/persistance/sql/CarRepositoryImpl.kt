package test.ktortemplate.core.persistance.sql

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.conf.database.DatabaseConnection
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.persistance.CarRepository
import test.ktortemplate.core.utils.pagination.PageRequest
import test.ktortemplate.core.utils.pagination.createSorts
import test.ktortemplate.core.utils.pagination.fromFilters

class CarRepositoryImpl : CarRepository, KoinComponent {

    private val dbc: DatabaseConnection by inject()

    override suspend fun getById(id: Long): Car? {
        return dbc.suspendedQuery {
            val rst = CarMappingsTable.select { CarMappingsTable.id eq id }.singleOrNull()
            if (rst != null) {
                resultToModel(rst)
            } else {
                null
            }
        }
    }

    override suspend fun save(car: CarSaveCommand): Car {
        return dbc.suspendedQuery {
            val newCarId = CarMappingsTable.insert {
                it[brand] = car.brand
                it[model] = car.model
            } get CarMappingsTable.id

            Car(newCarId.value, car.brand, car.model)
        }
    }

    override suspend fun count(pageRequest: PageRequest): Int {
        return dbc.suspendedQuery {
            CarMappingsTable.fromFilters(pageRequest.filter).count().toInt()
        }
    }

    override suspend fun delete(id: Long) {
        dbc.suspendedQuery {
            CarMappingsTable.deleteWhere { CarMappingsTable.id eq id }
        }
    }

    override suspend fun list(pageRequest: PageRequest): List<Car> {
        return dbc.suspendedQuery {
            CarMappingsTable
                .fromFilters(pageRequest.filter)
                .limit(pageRequest.limit, pageRequest.offset.toLong())
                .orderBy(*CarMappingsTable.createSorts(pageRequest.sort).toTypedArray())
                .map { resultToModel(it) }
        }
    }

    private fun resultToModel(rstRow: ResultRow): Car {
        return Car(
            rstRow[CarMappingsTable.id].value,
            rstRow[CarMappingsTable.brand],
            rstRow[CarMappingsTable.model]
        )
    }
}
