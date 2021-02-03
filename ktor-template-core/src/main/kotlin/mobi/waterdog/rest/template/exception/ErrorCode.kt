package mobi.waterdog.rest.template.exception

import io.ktor.http.HttpStatusCode

class ErrorCode(val httpStatusCode: HttpStatusCode, val messageCode: String)

object ErrorCodes {
    val InvalidUserInput = ErrorCode(HttpStatusCode.BadRequest, "client_error.invalid_parameters")
    val NotFound = ErrorCode(HttpStatusCode.NotFound, "client_error.not_found")

    // server errors
    val NotImplemented = ErrorCode(HttpStatusCode.NotImplemented, "server_error.not_implemented")
}
