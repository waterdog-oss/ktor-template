/**
 * Adapted from https://github.com/zensum/ktor-health-check
 */
package test.ktortemplate.core.utils.healthcheck

import io.ktor.http.HttpStatusCode
import test.ktortemplate.core.utils.json.JsonSettings

internal suspend fun healthCheck(fn: suspend () -> Map<String, Boolean>) = fn().let {
    val success = it.values.all { it }
    val json = JsonSettings.toJson(it)
    val status = if (success) {
        HttpStatusCode.OK
    } else {
        HttpStatusCode.InternalServerError
    }
    status to json
}
