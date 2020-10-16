package test.ktortemplate.core.exception

class AppException(
    val code: ErrorCode,
    val title: String? = null,
    val errors: MutableList<ErrorDefinition> = mutableListOf()) : Exception() {

    override fun toString(): String {
        return "AppException(code=$code, title=$title, errors=$errors)"
    }
}