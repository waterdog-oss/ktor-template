package test.ktortemplate.core.utils.pagination

import org.jetbrains.exposed.sql.AutoIncColumnType
import org.jetbrains.exposed.sql.BooleanColumnType
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.compoundAnd
import org.jetbrains.exposed.sql.compoundOr
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

/**
 * Creates exposed query from list of FilterField
 */
fun Table.createFromFilters(filters: List<FilterField>): Query {
    val filtersOperations = this.createFilters(filters)
    return if (filtersOperations.isEmpty()) this.selectAll() else this.select { filtersOperations.compoundAnd() }
}

/**
 * Creates list of exposed operations from list of FilterField.
 * Add support to more fields when needed.
 */
fun Table.createFilters(filters: List<FilterField>): List<Op<Boolean>> {
    return filters.map { filterField ->
        val column = this.columns.single { it.name == filterField.field }
        when (column.columnType) {
            is LongColumnType, is AutoIncColumnType -> {
                column as Column<Long>
                filterField.values.map { value -> Op.build { column.eq(value.toLong()) } }.compoundOr()
            }
            is IntegerColumnType -> {
                column as Column<Int>
                filterField.values.map { value -> Op.build { column.eq(value.toInt()) } }.compoundOr()
            }
            is VarCharColumnType, is TextColumnType -> {
                column as Column<String>
                filterField.values.map { value -> Op.build { column.eq(value) } }.compoundOr()
            }
            is BooleanColumnType -> {
                column as Column<Boolean>
                Op.build { column.eq(filterField.values.first().toBoolean()) }
            }
            else -> throw NotImplementedError("Column ${column.columnType} is not implemented")
        }
    }
}

/**
 * Creates list of exposed order expressions from list of SortField.
 */
fun Table.createSorts(sorts: List<SortField>): List<Pair<Expression<*>, SortOrder>> {
    return sorts.map { sortField ->
        Pair(
            this.columns.single { it.name == sortField.field },
            if (sortField.order == SortFieldOrder.asc) SortOrder.ASC else SortOrder.DESC
        )
    }
}
