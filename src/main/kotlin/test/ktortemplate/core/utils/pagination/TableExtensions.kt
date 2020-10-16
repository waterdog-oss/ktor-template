package test.ktortemplate.core.utils.pagination

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.compoundAnd
import org.jetbrains.exposed.sql.compoundOr
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

/**
 * Method that forces cast from Column<*> to Column<T>
 */
fun <T> Column<*>.asType(): Column<T> {
    @Suppress("UNCHECKED_CAST")
    return this as Column<T>
}

/**
 * Creates query from list of consumer filters.
 */
fun Table.fromFilters(filters: List<FilterField>): Query {
    val filtersOperations = this.createFilters(filters)
    return if (filtersOperations.isEmpty()) this.selectAll() else this.select { filtersOperations.compoundAnd() }
}

/**
 * Creates list of database operations from a list of consumer filters.
 * Using columnType.valueFromDB() to get the underlying column data type.
 */
fun Table.createFilters(filters: List<FilterField>): List<Op<Boolean>> {
    return filters.map { filterField ->
        val column: Column<*> = this.columns.single { it.name == filterField.field }
        val valueFromDB = column.columnType.valueFromDB(filterField.values.first()).let {
            when (it) {
                is EntityID<*> -> it.value
                else -> it
            }
        }

        // Support to more types can be added as needed bellow
        when (valueFromDB) {
            is Long -> column.asType<Long>().let {
                filterField.values.map { value -> Op.build { it.eq(value.toLong()) } }.compoundOr()
            }
            is Int -> column.asType<Int>().let {
                filterField.values.map { value -> Op.build { it.eq(value.toInt()) } }.compoundOr()
            }
            is String -> column.asType<String>().let {
                filterField.values.map { value -> Op.build { it.eq(value) } }.compoundOr()
            }
            is Boolean -> column.asType<Boolean>().let {
                Op.build { it.eq(filterField.values.first().toBoolean()) }
            }
            is UUID -> column.asType<UUID>().let {
                filterField.values.map { value -> Op.build { it.eq(UUID.fromString(value)) } }.compoundOr()
            }
            else -> throw NotImplementedError("Column ${column.columnType} is not implemented")
        }
    }
}

/**
 * Creates list of order expressions from list of consumer sorts.
 */
fun Table.createSorts(sorts: List<SortField>): List<Pair<Expression<*>, SortOrder>> {
    return sorts.map { sortField ->
        Pair(
            this.columns.single { it.name == sortField.field },
            if (sortField.order == SortFieldOrder.asc) SortOrder.ASC else SortOrder.DESC
        )
    }
}
