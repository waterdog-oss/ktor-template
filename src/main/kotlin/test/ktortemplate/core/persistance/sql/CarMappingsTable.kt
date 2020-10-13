package test.ktortemplate.core.persistance.sql

import org.jetbrains.exposed.dao.id.LongIdTable

internal object CarMappingsTable : LongIdTable("car") {
    val brand = text("brand")
    val model = text("model")
}
