package mobi.waterdog.rest.template.core.utils.exception

class AppException(
    val code: ErrorCode,
    val title: String? = null,
    val errors: MutableList<ErrorDefinition> = mutableListOf()
) : Exception()
