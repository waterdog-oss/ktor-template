package test.ktortemplate.core.exception

class AppException(
    val code: ErrorCode,
    val id: Any? = null,
    val title: String? = null,
    val params: Map<String, List<String>>? = null,
    val payload: Any? = null) : Exception() {
}