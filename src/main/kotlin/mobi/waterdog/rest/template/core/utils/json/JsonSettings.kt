package mobi.waterdog.rest.template.core.utils.json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object JsonSettings {

    val mapper = Json {
        encodeDefaults = false
        ignoreUnknownKeys = false
        isLenient = false
        allowStructuredMapKeys = false
        prettyPrint = false
        coerceInputValues = false
        classDiscriminator = "type"
        allowSpecialFloatingPointValues = false
    }

    inline fun <reified T> fromJson(json: String?): T {
        requireNotNull(json) { "String should not be null" }
        return mapper.decodeFromString(json)
    }

    inline fun <reified T> toJson(value: T?): String {
        requireNotNull(value) { "Value should not be null" }
        return mapper.encodeToString(value)
    }
}
