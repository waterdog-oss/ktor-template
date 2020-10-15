package test.ktortemplate.core.utils.versioning

import io.ktor.features.BadRequestException
import io.ktor.http.BadContentTypeFormatException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.parseAndSortContentTypeHeader
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext

fun Route.acceptVersion(version: ApiContentType, build: Route.() -> Unit): Route {
    val selector = HttpAcceptSameRouteSelector(version.contentType)
    return createChild(selector).apply(build)
}

data class HttpAcceptSameRouteSelector(val contentType: ContentType) :
    RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        val acceptHeaderContent = context.call.request.headers[HttpHeaders.Accept]
        try {
            val parsedHeaders = parseAndSortContentTypeHeader(acceptHeaderContent)
            if (parsedHeaders.isEmpty()) {
                return RouteSelectorEvaluation.Missing
            }

            // Checking if contentType is exactly the same
            val header = parsedHeaders.firstOrNull { contentType.toString() == it.value }
            if (header != null) {
                return RouteSelectorEvaluation(true, header.quality)
            }

            return RouteSelectorEvaluation.Failed
        } catch (failedToParse: BadContentTypeFormatException) {
            throw BadRequestException("Illegal Accept header format: $acceptHeaderContent", failedToParse)
        }
    }

    override fun toString(): String = "(contentType:$contentType)"
}
