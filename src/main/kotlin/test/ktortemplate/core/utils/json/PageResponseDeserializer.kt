package test.ktortemplate.core.utils.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import test.ktortemplate.core.utils.pagination.PageResponse
import test.ktortemplate.core.utils.pagination.PageResponseLink
import test.ktortemplate.core.utils.pagination.PageResponseMeta
import java.lang.reflect.Type
import java.util.ArrayList

class PageResponseDeserializer<T>(private val clazz: Class<T>) : JsonDeserializer<PageResponse<T>> {
    @Throws(JsonParseException::class)

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): PageResponse<T> {
        val metaObject = json.asJsonObject.getAsJsonObject("meta")
        val meta = JsonSettings.mapper.fromJson(metaObject, PageResponseMeta::class.java)

        val linksObject = json.asJsonObject.getAsJsonObject("links")
        val links = JsonSettings.mapper.fromJson(linksObject, PageResponseLink::class.java)

        val data = json.asJsonObject.getAsJsonArray("data")
        val list: MutableList<T> = ArrayList()
        data.forEach {
            list.add(context.deserialize(it, clazz))
        }

        return PageResponse(meta, list, links)
    }
}
