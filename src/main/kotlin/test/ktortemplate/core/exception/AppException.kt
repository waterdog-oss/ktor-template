package test.ktortemplate.core.exception

class AppException(
    val code: ErrorCode,
    val id: Any? = null,
    val title: String? = null,
    val params: MutableMap<String, List<String>> = mutableMapOf(),
    val payload: Any? = null) : Exception()