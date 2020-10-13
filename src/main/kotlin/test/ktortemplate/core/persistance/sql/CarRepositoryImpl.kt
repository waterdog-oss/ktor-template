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
import test.ktortemplate.core.utils.createFromFilters
import test.ktortemplate.core.utils.createSorts
import test.ktortemplate.core.utils.pagination.PageRequest

class CarRepositoryImpl : CarRepository, KoinComponent {

    private val dbc: DatabaseConnection by inject()

    override fun getById(id: Long): Car? {
        return dbc.query {
            val rst = CarMappingsTable.select { CarMappingsTable.id eq id }.singleOrNull()
            if (rst != null) {
                resultToModel(rst)
            } else {
                null
            }
        }
    }

    override fun save(car: CarSaveCommand): Car {
        return dbc.query {
            val newCarId = CarMappingsTable.insert {
                it[brand] = car.brand
                it[model] = car.model
            } get CarMappingsTable.id

            Car(newCarId, car.brand, car.model)
        }
    }

    override fun count(pageRequest: PageRequest): Int {
        return dbc.query {
            CarMappingsTable.createFromFilters(pageRequest.filter).count()
        }
    }

    override fun delete(id: Long) {
        dbc.query {
            CarMappingsTable.deleteWhere { CarMappingsTable.id eq id }
        }
    }

    override fun list(pageRequest: PageRequest): List<Car> {
        return dbc.query {
            CarMappingsTable
                .createFromFilters(pageRequest.filter)
                .limit(pageRequest.limit, pageRequest.offset)
                .orderBy(*CarMappingsTable.createSorts(pageRequest.sort).toTypedArray())
                .map { resultToModel(it) }
        }
    }

    private fun resultToModel(rstRow: ResultRow): Car {
        return Car(
            rstRow[CarMappingsTable.id],
            rstRow[CarMappingsTable.brand],
            rstRow[CarMappingsTable.model]
        )
    }
}
