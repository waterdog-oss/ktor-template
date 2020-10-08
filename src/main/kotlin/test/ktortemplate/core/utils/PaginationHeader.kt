package test.ktortemplate.core.utils

import io.ktor.application.ApplicationCall
import io.ktor.request.path
import io.ktor.response.header
import kotlin.math.ceil

data class HeaderParameter(val name: String, val value: String)

enum class SortFieldOrder(val prefix: String) {
    asc(""),
    desc("-")
}

data class SortField(
    val field: String,
    val order: SortFieldOrder
)

data class PageRequest(
    val page: Int,
    val size: Int,
    val sort: List<SortField>,
    val totalElements: Int,
) {
    val totalPages: Int = ceil(totalElements.toDouble() / size.toDouble()).toInt()
    val limit: Int = size // alias for size
    val offset: Int = page * size
    val firstPage: Int = 0
    val lastPage: Int = totalPages - 1
    val first: Boolean = page == firstPage
    val last: Boolean = page == lastPage
    val previousPage: Int = if (first) 0 else if ((page - 1) in firstPage..lastPage) page - 1 else firstPage
    val nextPage: Int = if (last) page else if ((page + 1) in firstPage..lastPage) page + 1 else lastPage
}

object PaginationHeader {
    private const val prefix = "vnd.ktortemplate.pagination"
    const val PAGE_NUMBER = "page[number]"
    const val PAGE_SIZE = "page[size]"
    const val PAGE_SORT = "sort"
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

    fun buildHeaders(pageInfo: PageRequest, path: String = ""): List<HeaderParameter> {
        return mutableListOf(
            HeaderParameter(HEADER_PAGE, pageInfo.page.toString()),
            HeaderParameter(HEADER_SIZE, pageInfo.size.toString()),
            HeaderParameter(HEADER_TOTAL_ELEMENTS, pageInfo.totalElements.toString()),
            HeaderParameter(HEADER_TOTAL_PAGES, pageInfo.totalPages.toString()),
            HeaderParameter(HEADER_FIRST, pageInfo.first.toString()),
            HeaderParameter(HEADER_LAST, pageInfo.last.toString()),
            HeaderParameter(
                HEADER_LINKS_SELF,
                buildPageLink(pageInfo.page, pageInfo.size, pageInfo.sort, path)
            ),
            HeaderParameter(
                HEADER_LINKS_FIRST,
                buildPageLink(pageInfo.firstPage, pageInfo.size, pageInfo.sort, path)
            ),
            HeaderParameter(
                HEADER_LINKS_PREV,
                buildPageLink(pageInfo.previousPage, pageInfo.size, pageInfo.sort, path)
            ),
            HeaderParameter(
                HEADER_LINKS_NEXT,
                buildPageLink(pageInfo.nextPage, pageInfo.size, pageInfo.sort, path)
            ),
            HeaderParameter(
                HEADER_LINKS_LAST,
                buildPageLink(pageInfo.lastPage, pageInfo.size, pageInfo.sort, path)
            ),
        )
    }

    /**
     * ?page[number]=0&page[size]=5&sort=-id,brand
     */
    private fun buildPageLink(page: Int, size: Int, sort: List<SortField>, path: String = ""): String {
        val sortStr =
            if (sort.isNotEmpty()) "&$PAGE_SORT=${sort.joinToString(",") { "${it.order.prefix}${it.field}" }}" else ""
        return "$path?$PAGE_NUMBER=$page&$PAGE_SIZE=${size}$sortStr"
    }
}

/**
 * Generates page info from call parameters and adds info as a response header.
 * Format: ?page[number]=0&page[size]=5&sort=-id,brand
 */
fun ApplicationCall.parsePageRequest(
    totalElements: Int,
    addResponseHeaders: Boolean = false
): PageRequest {
    val page = parameters[PaginationHeader.PAGE_NUMBER]?.toInt() ?: 0
    val size = parameters[PaginationHeader.PAGE_SIZE]?.toInt() ?: 10
    val sort = parameters[PaginationHeader.PAGE_SORT]?.let { fields ->
        // Format: sort=-id,brand
        fields.split(",").map { field ->
            when (field.startsWith(SortFieldOrder.desc.prefix)) {
                true -> SortField(field = field.substring(1), order = SortFieldOrder.desc)
                false -> SortField(field = field, order = SortFieldOrder.asc)
            }
        }
    } ?: listOf()

    val pageInfo = PageRequest(
        page = page,
        size = size,
        sort = sort,
        totalElements = totalElements
    )

    // Add pagination headers to response
    if (addResponseHeaders) {
        val headers = PaginationHeader.buildHeaders(pageInfo, request.path())
        headers.forEach { response.header(it.name, it.value) }
    }

    return pageInfo
}
