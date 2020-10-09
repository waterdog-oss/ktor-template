package test.ktortemplate.core.model

enum class SortFieldOrder {
    asc,
    desc
}

data class SortField(
    val field: String,
    val order: SortFieldOrder
)

/**
 * If multiple values are provided OR operator should be used for the field
 */
data class FilterField(
    val field: String,
    val values: List<String>
)

data class PageRequest(
    val page: Int,
    val size: Int,
    val sort: List<SortField>,
    val filter: List<FilterField>
) {
    val limit: Int = size // alias for size
    val offset: Int = page * size

    companion object {
        fun default() = PageRequest(
            page = 0,
            size = 10,
            sort = listOf(),
            filter = listOf()
        )
    }
}
