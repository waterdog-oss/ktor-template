package test.ktortemplate.core.utils.versioning

import io.ktor.http.ContentType

object ApiVersion {
    private const val prefix = "vnd.ktortemplate"

    enum class JSON(val contentType: ContentType) {
        V1(ContentType("application", "$prefix.v1+json")),
        V2(ContentType("application", "$prefix.v2+json")),
        Latest(V2.contentType)
    }

    enum class XML(val contentType: ContentType) {
        V1(ContentType("application", "$prefix.v1+xml")),
        Latest(V1.contentType)
    }
}