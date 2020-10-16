package test.ktortemplate.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Car(val id: Long, val brand: String, val model: String)

@Serializable
data class CarSaveCommand(val brand: String, val model: String)
