package test.ktortemplate.core.model

data class Part(val partNo: Long, val manufacturer: String, val description: String)

data class RegisterPartReplacementCommand(val carId: Long, val parts: List<Part>)
