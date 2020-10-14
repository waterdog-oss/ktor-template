package test.ktortemplate.core.utils.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import test.ktortemplate.core.model.Car
import test.ktortemplate.core.utils.pagination.PageResponse
import java.lang.reflect.Type

object JsonSettings {

    val PageResponseCarType: Type = object : TypeToken<PageResponse<Car>>() {}.type

    val mapper = GsonBuilder().apply {
        serializeNulls()
        setPrettyPrinting()
        registerTypeAdapter(PageResponseCarType, PageResponseDeserializer(Car::class.java))
    }.create()!!
}

// Helper function that infers class type. Not necessary, but nice to have
// val car: Car = JsonSettings.mapper.fromJson(response.content)
inline fun <reified T> Gson.fromJson(value: String?) = this.fromJson(value, T::class.java)
