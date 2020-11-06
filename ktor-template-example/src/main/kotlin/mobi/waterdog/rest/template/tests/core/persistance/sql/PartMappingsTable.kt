package mobi.waterdog.rest.template.tests.core.persistance.sql

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

internal object PartMappingsTable : Table("parts") {
    val partNo = long("part_no")
    val carId = reference("car_id", CarMappingsTable.id, ReferenceOption.CASCADE)
    val manufacturer = varchar("manufacturer", length = 255)
    val description = text("description")

    override val primaryKey = PrimaryKey(partNo)

    init {
        uniqueIndex(partNo, carId)
    }
}
