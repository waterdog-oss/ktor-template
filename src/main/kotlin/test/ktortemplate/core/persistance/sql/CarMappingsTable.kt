package test.ktortemplate.core.persistance.sql

import org.jetbrains.exposed.dao.id.LongIdTable

internal object CarMappingsTable : LongIdTable("cars") {
    val brand = varchar("brand", length = 255)
    val model = varchar("model", length = 255)
}
