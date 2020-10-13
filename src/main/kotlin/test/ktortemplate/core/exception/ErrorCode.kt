package test.ktortemplate.core.exception

import io.ktor.http.*

enum class ErrorCode(val httpStatusCode: HttpStatusCode, val messageCode: String) {
    InvalidUserInput(HttpStatusCode.BadRequest, "client_error.invalid_parameters"),
    NotFound(HttpStatusCode.NotFound, "client_error.not_found"),

    NotImplemented(HttpStatusCode.NotImplemented, "server_error.not_implemented")

    // TODO fill with all possible app errors
}