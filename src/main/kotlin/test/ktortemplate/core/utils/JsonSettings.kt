package test.ktortemplate.core.utils

import kotlinx.serialization.json.Json

object JsonSettings {
    val mapper = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
}
