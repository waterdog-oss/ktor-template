package test.ktortemplate.core.utils.log

import ch.qos.logback.contrib.json.JsonFormatter
import ch.qos.logback.contrib.json.classic.JsonLayout
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * Semi-structured formatter for logs:
 * 2020-10-28T10:28:03.564Z INFO  258f7191-db2e-4cc6-8af6-627c9e8e066e Ktor server started...   {...}
 */
class SemiStructuredLogFormatter : JsonFormatter {
    companion object {
        const val REQUEST_ID_HEADER = "X-Request-Id"
    }

    // TODO: Replace by kotlinx-serialization
    private val mapper = jacksonObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .disable(SerializationFeature.INDENT_OUTPUT)

    internal data class LogResponse(
        val timestamp: String,
        val level: String,
        val thread: String,
        val logger: String,
        val message: String,
        val exception: String? = null,
        val metadata: Map<String, String>? = null
    )

    override fun toJsonString(map: Map<*, *>): String {
        // Using internal class because kotlinx-serialization does not support Map<*, *> serialization:
        // "Star projections in type arguments are not allowed, but had Map<*, *>"
        val json = LogResponse(
            timestamp = map[JsonLayout.TIMESTAMP_ATTR_NAME]?.toString() ?: "",
            level = map[JsonLayout.LEVEL_ATTR_NAME]?.toString() ?: "",
            thread = map[JsonLayout.THREAD_ATTR_NAME]?.toString() ?: "",
            logger = map[JsonLayout.LOGGER_ATTR_NAME]?.toString() ?: "",
            message = map[JsonLayout.FORMATTED_MESSAGE_ATTR_NAME]?.toString() ?: "",
            exception = map[JsonLayout.EXCEPTION_ATTR_NAME]?.toString(),
            metadata = map[JsonLayout.MDC_ATTR_NAME]?.let {
                @Suppress("UNCHECKED_CAST")
                it as Map<String, String>
            }
        )
        val requestId = json.metadata?.get(REQUEST_ID_HEADER)

        // Construct log message
        var str = ""
        str += "${json.timestamp} "
        str += "${json.level} ".padEnd(6)
        str += if (requestId != null && requestId.isNotEmpty()) "$requestId " else ""
        str += "${json.message} "
        str = str.padEnd(125) // add padding to the first fields for better readability
        str += if (json.exception != null && json.exception.isNotEmpty()) "${json.exception} " else ""
        str += mapper.writeValueAsString(json)
        str += "\n"

        return str
    }
}
