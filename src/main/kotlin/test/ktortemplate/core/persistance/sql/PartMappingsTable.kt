package test.ktortemplate.core.persistance.sql

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

internal object PartMappingsTable : Table("part") {
    val partNo = long("part_no")
    val carId = reference("car_id", CarMappingsTable.id, ReferenceOption.CASCADE)
    val manufacturer = text("manufacturer")
    val desc = text("desc")

    override val primaryKey = PrimaryKey(partNo)

    init {
        uniqueIndex(partNo, carId)
    }
}
