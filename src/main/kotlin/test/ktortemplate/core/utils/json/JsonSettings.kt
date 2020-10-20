package test.ktortemplate.core.utils.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonSettings {

    val mapper: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    inline fun <reified T> fromJson(json: String?): T {
        val jsonAdapter: JsonAdapter<T> = mapper.adapter(T::class.java)
        return jsonAdapter.fromJson(json!!)!!
    }

    inline fun <reified T> toJson(value: T?): String {
        val jsonAdapter: JsonAdapter<T> = mapper.adapter(T::class.java)
        return jsonAdapter.toJson(value)
    }

    inline fun <reified T> fromJson(json: String?, customAdapter: JsonAdapter<T>): T {
        return customAdapter.fromJson(json!!)!!
    }

    fun <T> toJson(value: T?, customAdapter: JsonAdapter<T>): String {
        return customAdapter.toJson(value)
    }
}
