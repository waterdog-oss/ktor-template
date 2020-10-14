package test.ktortemplate.core.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object JsonSettings {

    val mapper = GsonBuilder().apply {
        serializeNulls()
        setPrettyPrinting()
    }.create()!!

    // Helper function that infers class type. Not necessary, but nice to have
    // val car: Car = JsonSettings.mapper.fromJson(response.content)
    inline fun <reified T> Gson.fromJson(value: String?) = this.fromJson(value, T::class.java)
}
