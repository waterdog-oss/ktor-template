package test.ktortemplate.core.utils.json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object JsonSettings {

    val mapper = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
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
