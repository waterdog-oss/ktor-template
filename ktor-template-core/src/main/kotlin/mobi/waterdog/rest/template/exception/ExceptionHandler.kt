package mobi.waterdog.rest.template.exception

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

fun StatusPagesConfig.defaultExceptionHandler() {
    val log = LoggerFactory.getLogger(this::class.java)

    exception<AppException> { call, cause ->
        val errorData = ErrorDTO(cause.code.httpStatusCode.value, cause.code.messageCode, cause.title, cause.errors)
        log.error("Returning AppException data to client:\n$errorData", cause)
        call.respond(cause.code.httpStatusCode, errorData)
    }

    exception<Throwable> { call, cause ->
        log.error("Unexpected exception.", cause)
        call.respond(
            HttpStatusCode.InternalServerError,
            ErrorDTO(
                httpStatusCode = HttpStatusCode.InternalServerError.value,
                title = cause.localizedMessage
            )
        )
    }
}

fun StatusPagesConfig.defaultStatusCodes() {
    status(HttpStatusCode.NotFound) { call, _ -> call.respond(HttpStatusCode.NotFound, ErrorDTO(HttpStatusCode.NotFound.value)) }
    // TODO customize response for some status codes if necessary
}
