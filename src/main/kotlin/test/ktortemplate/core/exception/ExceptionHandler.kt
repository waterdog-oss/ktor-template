package test.ktortemplate.core.exception

import com.fasterxml.jackson.annotation.JsonInclude
import io.ktor.application.*
import io.ktor.features.StatusPages.*
import io.ktor.http.*
import io.ktor.response.*
import java.util.*

/**
 * Intended to be the result of an erroneous http call.
 */
private data class ErrorDTO (
    val httpStatusCode: Int,
    val messageCode: String? = null,
    val title: String? = null,
    val errors: List<ErrorDefinition> = listOf()
) {
    val id: UUID = UUID.randomUUID()
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorDefinition(val errorCode: String, val field: String?, val args: Map<String, String>) {
    override fun toString(): String {
        return "ErrorDefinition(errorCode='$errorCode', field=$field, args=$args)"
    }
}


fun Configuration.appException() {
    exception<AppException> {
        val errorData = ErrorDTO(it.code.httpStatusCode.value, it.code.messageCode, it.title, it.errors)
        // TODO log

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

    // TODO customize response for some status codes if necessary

    status(HttpStatusCode.NotFound) { call.respond(ErrorDTO(HttpStatusCode.NotFound.value)) }
}