package test.ktortemplate.core.exception

import io.ktor.application.*
import io.ktor.features.StatusPages.*
import io.ktor.http.*
import io.ktor.response.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Configuration.appException() {

    exception<AppException> {
        val errorData = ErrorDTO(it.code.httpStatusCode.value, it.code.messageCode, it.title, it.errors)
        log.error("Returning AppException data to client:\n${errorData}")
        call.respond(it.code.httpStatusCode, errorData)
    }

    exception<Throwable> {
        call.respond(
            ErrorDTO(
                httpStatusCode = HttpStatusCode.InternalServerError.value,
                title = it.localizedMessage))
    }
}

fun Configuration.defaultStatusCodes() {
    status(HttpStatusCode.NotFound) { call.respond(ErrorDTO(HttpStatusCode.NotFound.value)) }
    // TODO customize response for some status codes if necessary
}

val log: Logger get() = LoggerFactory.getLogger(Configuration::class.java)
