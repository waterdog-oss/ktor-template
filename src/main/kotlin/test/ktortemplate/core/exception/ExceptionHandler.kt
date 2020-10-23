package test.ktortemplate.core.exception

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

fun StatusPages.Configuration.appException() {

    exception<AppException> {
        val requestId = MDC.get("X-Request-Id")
        val errorData = ErrorDTO(it.code.httpStatusCode.value, it.code.messageCode, it.title, requestId, it.errors)
        log.error("Returning AppException data to client (X-Request-Id=$requestId):\n$errorData")
        call.respond(it.code.httpStatusCode, errorData)
    }

    exception<Throwable> {
        val requestId = MDC.get("X-Request-Id")
        log.error("Returning exception (X-Request-Id=$requestId)")
        call.respond(
            ErrorDTO(
                httpStatusCode = HttpStatusCode.InternalServerError.value,
                title = it.localizedMessage,
                requestId = requestId
            )
        )
    }
}

fun StatusPages.Configuration.defaultStatusCodes() {
    status(HttpStatusCode.NotFound) {
        val requestId = MDC.get("X-Request-Id")
        call.respond(
            HttpStatusCode.NotFound,
            ErrorDTO(httpStatusCode = HttpStatusCode.NotFound.value, requestId = requestId)
        )
    }
    // TODO customize response for some status codes if necessary
}

val log: Logger get() = LoggerFactory.getLogger(StatusPages.Configuration::class.java)
