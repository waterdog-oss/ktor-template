package test.ktortemplate.core.exception

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID

/**
 * Intended to be the result of an erroneous http call.
 */
data class ErrorDTO(
    val httpStatusCode: Int,
    val messageCode: String? = null,
    val title: String? = null,
    val requestId: String? = null,
    val errors: List<ErrorDefinition> = listOf()
) {
    val id: UUID = UUID.randomUUID()

    override fun toString(): String {
        return "ErrorDTO(id=$id, httpStatusCode=$httpStatusCode, messageCode=$messageCode, title=$title, errors=$errors)"
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorDefinition(val errorCode: String, val field: String?, val args: Map<String, String>) {
    override fun toString(): String {
        return "ErrorDefinition(errorCode='$errorCode', field=$field, args=$args)"
    }
}
