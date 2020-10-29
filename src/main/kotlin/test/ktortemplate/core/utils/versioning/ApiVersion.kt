package test.ktortemplate.core.utils.versioning

/**
 * Enum that holds application versions
 */
enum class ApiVersion(val version: String) {
    V1("v1"),
    Latest(V1.version);

    override fun toString(): String = this.version
}
