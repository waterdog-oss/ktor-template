package test.ktortemplate.core.model

data class Car(val id: Long, val brand: String, val model: String)

data class CarSaveCommand(val brand: String, val model: String)

data class CarSaveCommandV1(val brand: String)
