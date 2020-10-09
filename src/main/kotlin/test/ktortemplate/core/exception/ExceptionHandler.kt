package test.ktortemplate.core.exception

import io.ktor.application.*
import io.ktor.features.StatusPages.*
import io.ktor.http.*
import io.ktor.response.*

private data class ErrorData (
    val httpStatusCode: Int,
    val messageCode: String? = null,
    val id: Any? = null,
    val title: String? = null,
    val params: Map<String, List<String>>? = null,
    val payload: Any? = null
)

fun Configuration.appException() {

    exception<AppException> {
        call.respond(
            it.code.httpStatusCode,
            ErrorData(it.code.httpStatusCode.value, it.code.messageCode, it.id, it.title, it.params, it.payload))
    }

    exception<Throwable> {
        call.respond(
            ErrorData(
                httpStatusCode = HttpStatusCode.InternalServerError.value,
                title = it.localizedMessage,
                payload = it.asString ))
    }
}

fun Configuration.defaultStatusCodes() {

    // TODO customize response for some status codes if necessary

    status(HttpStatusCode.NotFound) { call.respond(ErrorData(HttpStatusCode.NotFound.value)) }
}