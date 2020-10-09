package test.ktortemplate.core.utils

import io.ktor.application.ApplicationCall
import io.ktor.request.path
import io.ktor.response.header
import test.ktortemplate.core.model.FilterField
import test.ktortemplate.core.model.PageRequest
import test.ktortemplate.core.model.SortField
import test.ktortemplate.core.model.SortFieldOrder

data class HeaderParameter(val name: String, val value: String)

data class PageInfo(
    val page: Int,
    val size: Int,
    val sort: List<SortField>,
    val filter: List<FilterField>,
    val totalElements: Int,
) {
    val totalPages: Int = kotlin.math.ceil(totalElements.toDouble() / size.toDouble()).toInt()
    val firstPage: Int = 0
    val lastPage: Int = totalPages - 1
    val first: Boolean = page == firstPage
    val last: Boolean = page == lastPage
    val previousPage: Int = if ((page - 1) in firstPage..lastPage) page - 1 else firstPage
    val nextPage: Int = if ((page + 1) in firstPage..lastPage) page + 1 else lastPage
}

object PaginationHeader {
    private const val prefix = "vnd.ktortemplate.pagination"
    const val PAGE_NUMBER = "page[number]"
    const val PAGE_SIZE = "page[size]"
    const val PAGE_SORT = "sort"
    const val PAGE_FILTER = "filter"
    const val HEADER_PAGE = "$prefix.page"
    const val HEADER_SIZE = "$prefix.size"
    const val HEADER_TOTAL_ELEMENTS = "$prefix.total_elements"
    const val HEADER_TOTAL_PAGES = "$prefix.total_pages"
    const val HEADER_FIRST = "$prefix.first"
    const val HEADER_LAST = "$prefix.last"
    const val HEADER_LINKS_SELF = "$prefix.links.self"
    const val HEADER_LINKS_FIRST = "$prefix.links.first"
    const val HEADER_LINKS_PREV = "$prefix.links.prev"
    const val HEADER_LINKS_NEXT = "$prefix.links.next"
    const val HEADER_LINKS_LAST = "$prefix.links.last"

    fun buildPaginationHeaders(pageInfo: PageInfo, path: String = ""): List<HeaderParameter> {
        return mutableListOf(
            HeaderParameter(HEADER_PAGE, pageInfo.page.toString()),
            HeaderParameter(HEADER_SIZE, pageInfo.size.toString()),
            HeaderParameter(HEADER_TOTAL_ELEMENTS, pageInfo.totalElements.toString()),
            HeaderParameter(HEADER_TOTAL_PAGES, pageInfo.totalPages.toString()),
            HeaderParameter(HEADER_FIRST, pageInfo.first.toString()),
            HeaderParameter(HEADER_LAST, pageInfo.last.toString()),
            HeaderParameter(
                HEADER_LINKS_SELF,
                buildPaginationLink(pageInfo.page, pageInfo.size, pageInfo.sort, pageInfo.filter, path)
            ),
            HeaderParameter(
                HEADER_LINKS_FIRST,
                buildPaginationLink(pageInfo.firstPage, pageInfo.size, pageInfo.sort, pageInfo.filter, path)
            ),
            HeaderParameter(
                HEADER_LINKS_PREV,
                buildPaginationLink(pageInfo.previousPage, pageInfo.size, pageInfo.sort, pageInfo.filter, path)
            ),
            HeaderParameter(
                HEADER_LINKS_NEXT,
                buildPaginationLink(pageInfo.nextPage, pageInfo.size, pageInfo.sort, pageInfo.filter, path)
            ),
            HeaderParameter(
                HEADER_LINKS_LAST,
                buildPaginationLink(pageInfo.lastPage, pageInfo.size, pageInfo.sort, pageInfo.filter, path)
            ),
        )
    }

    /**
     * ?page[number]=0&page[size]=5&sort[id]=desc&sort[brand]=asc&filter[brand]=brand1,brand2&filter[model]=model1
     */
    private fun buildPaginationLink(
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
    val page = parameters[PaginationHeader.PAGE_NUMBER]?.toInt() ?: 0
    val size = parameters[PaginationHeader.PAGE_SIZE]?.toInt() ?: 10
    val sort = parameters.entries().filter { it.key.startsWith("${PaginationHeader.PAGE_SORT}[") }.map { entry ->
        // Format: sort[id]=desc&sort[brand]
        val field = entry.key.substring(entry.key.indexOf("[") + 1, entry.key.indexOf("]"))
        val order = SortFieldOrder.valueOf(entry.value.first()) // only the first entry is used
        SortField(field = field, order = order)
    }
    val filter = parameters.entries().filter { it.key.startsWith("${PaginationHeader.PAGE_FILTER}[") }.map { entry ->
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

/**
 * * Generates pagination headers and adds as response headers.
 */
fun ApplicationCall.generatePaginationHeaders(
    pageRequest: PageRequest,
    totalElements: Int
) {
    val pageInfo = PageInfo(
        page = pageRequest.page,
        size = pageRequest.size,
        sort = pageRequest.sort,
        filter = pageRequest.filter,
        totalElements = totalElements
    )

    val headers = PaginationHeader.buildPaginationHeaders(pageInfo, request.path())
    headers.forEach { response.header(it.name, it.value) }
}
