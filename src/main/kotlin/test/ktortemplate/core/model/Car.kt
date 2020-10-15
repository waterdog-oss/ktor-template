package test.ktortemplate.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Car(val id: Long, val brand: String, val model: String)

@Serializable
data class CarSaveCommand(val brand: String, val model: String)

@Serializable
data class Test<T>(val list: List<T>, val car: Car?) {
    companion object{
        fun <T> from(): Test<T> {
            return Test<T>(listOf(), Car(1, "", ""))
        }
    }
}

