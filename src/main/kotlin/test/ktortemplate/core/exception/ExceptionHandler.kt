package test.ktortemplate.core.exception

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.slf4j.LoggerFactory

fun StatusPages.Configuration.appException() {
    val log = LoggerFactory.getLogger(this::class.java)

    exception<AppException> {
        val errorData = ErrorDTO(it.code.httpStatusCode.value, it.code.messageCode, it.title, it.errors)
        log.error("Returning AppException data to client:\n$errorData", it)
        call.respond(it.code.httpStatusCode, errorData)
    }

    exception<Throwable> {
        log.error("Unexpected exception.", it)
        call.respond(
            HttpStatusCode.InternalServerError,
            ErrorDTO(
                httpStatusCode = HttpStatusCode.InternalServerError.value,
                title = it.localizedMessage
            )
        )
    }
}

fun StatusPages.Configuration.defaultStatusCodes() {
    status(HttpStatusCode.NotFound) { call.respond(HttpStatusCode.NotFound, ErrorDTO(HttpStatusCode.NotFound.value)) }
    // TODO customize response for some status codes if necessary
}
