package test.ktortemplate.core.model

import com.fasterxml.jackson.annotation.JsonInclude
import test.ktortemplate.core.utils.PaginationUtils

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PageResponseMeta(
    val page: Int,
    val size: Int,
    val totalElements: Int
) {
    val totalPages: Int = kotlin.math.ceil(totalElements.toDouble() / size.toDouble()).toInt()
    val firstPage: Int = 0
    val lastPage: Int = totalPages - 1
    val first: Boolean = page == firstPage
    val last: Boolean = page == lastPage
    val previousPage: Int? = if ((page - 1) in firstPage..lastPage) page - 1 else null
    val nextPage: Int? = if ((page + 1) in firstPage..lastPage) page + 1 else null
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PageResponseLink(
    val self: String,
    val first: String,
    val prev: String?,
    val next: String?,
    val last: String
)

/**
 * Envelope for data responses with pagination
 */
data class PageResponse<T>(
    val meta: PageResponseMeta,
    val data: List<T>,
    val links: PageResponseLink
) {
    companion object {
        fun <T> from(pageRequest: PageRequest, totalElements: Int, data: List<T>, path: String): PageResponse<T> {
            val meta = PageResponseMeta(
                page = pageRequest.page,
                size = pageRequest.size,
                totalElements = totalElements
            )
            return PageResponse(
                meta = meta,
                data = data,
                links = PageResponseLink(
                    self = PaginationUtils.buildPaginationLink(
                        meta.page,
                        pageRequest.size,
                        pageRequest.sort,
                        pageRequest.filter,
                        path
                    ),
                    first = PaginationUtils.buildPaginationLink(
                        meta.firstPage,
                        pageRequest.size,
                        pageRequest.sort,
                        pageRequest.filter,
                        path
                    ),
                    prev = if (meta.previousPage != null) PaginationUtils.buildPaginationLink(
                        meta.previousPage,
                        pageRequest.size,
                        pageRequest.sort,
                        pageRequest.filter,
                        path
                    ) else null,
                    next = if (meta.nextPage != null) PaginationUtils.buildPaginationLink(
                        meta.nextPage,
                        pageRequest.size,
                        pageRequest.sort,
                        pageRequest.filter,
                        path
                    ) else null,
                    last = PaginationUtils.buildPaginationLink(
                        meta.lastPage,
                        pageRequest.size,
                        pageRequest.sort,
                        pageRequest.filter,
                        path
                    )
                )
            )
        }
    }
}
