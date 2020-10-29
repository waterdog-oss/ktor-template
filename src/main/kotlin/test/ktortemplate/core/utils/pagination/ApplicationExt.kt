package test.ktortemplate.core.utils.pagination

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import test.ktortemplate.core.utils.json.JsonSettings

suspend inline fun <reified T> ApplicationCall.respondPaged(message: PageResponse<T>) {
    response.pipeline.execute(this, TextContent(JsonSettings.toJson(message), ContentType.Application.Json))
}
