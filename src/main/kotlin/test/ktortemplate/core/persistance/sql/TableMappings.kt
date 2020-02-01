package test.ktortemplate.core.persistance.sql

import org.jetbrains.exposed.sql.Table

internal object CarMappingsTable : Table("car") {
    val id = long("id").primaryKey().autoIncrement()
    val brand = text("brand")
    val model = text("model")
}
