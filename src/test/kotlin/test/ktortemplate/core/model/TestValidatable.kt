package test.ktortemplate.core.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import test.ktortemplate.core.exception.AppException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestValidatable {

    @Test
    fun `Validating a car with an invalid brand`() {
        assertThrows<AppException> {
            Car(1, "ford", "Focus", listOf()).validate()
        }
    }

    @Test
    fun `Validating a car with a valid brand`() {
        Car(1, "koenigsegg", "cc", listOf()).validate()
    }

    @Test
    fun `Validate a list a with an invalid wheel definition`() {

        val wheels = listOf(
            Wheel(17, 225),
            Wheel(17, 255),
            Wheel(3, 225)) // <-- invalid diameter
        val exception = assertThrows<AppException> {
            Car(1, "porsche", "911", wheels).validate()
        }
        println(exception)
    }
}