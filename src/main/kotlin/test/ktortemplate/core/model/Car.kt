package test.ktortemplate.core.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Car(val id: Long, val brand: String, val model: String)

@JsonClass(generateAdapter = true)
data class CarSaveCommand(val brand: String, val model: String)
