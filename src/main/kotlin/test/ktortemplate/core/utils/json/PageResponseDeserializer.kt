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
        val meta = json.asJsonObject.getAsJsonObject("meta").let {
            JsonSettings.mapper.fromJson(it, PageResponseMeta::class.java)
        }
        val links = json.asJsonObject.getAsJsonObject("links").let {
            JsonSettings.mapper.fromJson(it, PageResponseLink::class.java)
        }

        val list: MutableList<T> = ArrayList()
        json.asJsonObject.getAsJsonArray("data").forEach {
            list.add(context.deserialize(it, clazz))
        }

        return PageResponse(meta, list, links)
    }
}
