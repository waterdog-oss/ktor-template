package test.ktortemplate.core.utils.versioning

import io.ktor.features.BadRequestException
import io.ktor.http.BadContentTypeFormatException
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext

/**
 * Route extension to accept methods depending on the specified format.
 *
 * acceptFormat(ApiFormat.json) {
 *   get("/resource") {
 *       ...
 *   }
 * }
 */
fun Route.acceptFormat(format: ApiFormat, build: Route.() -> Unit): Route {
    val selector = HttpAcceptFormatSelector(format)
    return createChild(selector).apply(build)
}

internal data class HttpAcceptFormatSelector(val format: ApiFormat) :
    RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        val acceptFormatContent = context.call.parameters["format"] ?: return RouteSelectorEvaluation.Missing

        try {
            if (ApiFormat.valueOf(acceptFormatContent) !== format) {
                return RouteSelectorEvaluation.Failed
            }

            return RouteSelectorEvaluation.Constant
        } catch (failedToParse: BadContentTypeFormatException) {
            throw BadRequestException("Illegal Accept format: $acceptFormatContent", failedToParse)
        }
    }

    override fun toString(): String = "(format:$format)"
}
