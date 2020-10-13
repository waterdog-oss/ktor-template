package test.ktortemplate.core.utils

import io.ktor.application.ApplicationCall
import test.ktortemplate.core.model.FilterField
import test.ktortemplate.core.model.PageRequest
import test.ktortemplate.core.model.SortField
import test.ktortemplate.core.model.SortFieldOrder

object PaginationUtils {
    const val PAGE_NUMBER = "page[number]"
    const val PAGE_SIZE = "page[size]"
    const val PAGE_SORT = "sort"
    const val PAGE_FILTER = "filter"

    /**
     * Link format:
     * ?page[number]=0&page[size]=5&sort[id]=desc&sort[brand]=asc&filter[brand]=brand1,brand2&filter[model]=model1
     */
    fun buildPaginationLink(
        page: Int,
        size: Int,
        sort: List<SortField>,
        filter: List<FilterField>,
        path: String = ""
    ): String {
        val sortStr =
            if (sort.isNotEmpty()) "&${sort.joinToString("&") { "$PAGE_SORT[${it.field}]=${it.order.name}" }}" else ""
        val filterStr = if (filter.isNotEmpty()) "&${
        filter.joinToString("&") {
            "$PAGE_FILTER[${it.field}]=${
            it.values.joinToString(",")
            }"
        }
        }" else ""
        return "$path?$PAGE_NUMBER=$page&$PAGE_SIZE=${size}$sortStr$filterStr"
    }
}

/**
 * Parses page request from call parameters
 */
fun ApplicationCall.parsePageRequest(): PageRequest {
    val page = parameters[PaginationUtils.PAGE_NUMBER]?.toInt() ?: 0
    val size = parameters[PaginationUtils.PAGE_SIZE]?.toInt() ?: 10
    val sort = parameters.entries().filter { it.key.startsWith("${PaginationUtils.PAGE_SORT}[") }.map { entry ->
        // Format: sort[id]=desc&sort[brand]
        val field = entry.key.substring(entry.key.indexOf("[") + 1, entry.key.indexOf("]"))
        val order = SortFieldOrder.valueOf(entry.value.first()) // only the first entry is used
        SortField(field = field, order = order)
    }
    val filter = parameters.entries().filter { it.key.startsWith("${PaginationUtils.PAGE_FILTER}[") }.map { entry ->
        // Format: filter[brand]=brand1,brand2&filter[model]=model1
        val field = entry.key.substring(entry.key.indexOf("[") + 1, entry.key.indexOf("]"))
        val values = entry.value.first().split(",") // only the first entry is used
        FilterField(field = field, values = values)
    }

    return PageRequest(
        page = page,
        size = size,
        sort = sort,
        filter = filter,
    )
}
