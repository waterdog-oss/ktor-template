package test.ktortemplate.core.utils.versioning

import io.ktor.http.ContentType

object ApiVersion {
    private const val prefix = "vnd.ktortemplate"

    enum class JSON(val contentType: ContentType) {
        v1(ContentType("application", "$prefix.v1+json")),
        v2(ContentType("application", "$prefix.v2+json")),
        latest(v2.contentType)
    }

    enum class XML(val contentType: ContentType) {
        v1(ContentType("application", "$prefix.v1+xml")),
        latest(v1.contentType)
    }
}