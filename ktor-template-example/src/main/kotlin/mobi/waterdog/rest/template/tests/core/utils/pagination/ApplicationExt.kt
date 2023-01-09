package mobi.waterdog.rest.template.tests.core.utils.pagination

import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.server.application.ApplicationCall
import mobi.waterdog.rest.template.pagination.PageResponse
import mobi.waterdog.rest.template.tests.core.utils.json.JsonSettings

suspend inline fun <reified T> ApplicationCall.respondPaged(message: PageResponse<T>) {
    response.pipeline.execute(this, TextContent(JsonSettings.toJson(message), ContentType.Application.Json))
}
