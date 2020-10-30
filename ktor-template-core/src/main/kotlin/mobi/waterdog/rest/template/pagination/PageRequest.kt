package mobi.waterdog.rest.template.pagination

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
    val page: Int = 0,
    val size: Int = 10,
    val sort: List<SortField> = listOf(),
    val filter: List<FilterField> = listOf()
) {
    val limit: Int = size // alias for size
    val offset: Int = page * size
}
