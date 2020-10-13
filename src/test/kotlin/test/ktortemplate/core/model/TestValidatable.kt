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
            Car(1, "ford", "Focus").validate()
        }
    }

    @Test
    fun `Validating a car with a valid brand`() {
        Car(1, "koenigsegg", "cc").validate()
    }
}