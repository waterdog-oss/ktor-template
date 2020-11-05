package mobi.waterdog.rest.template.tests.core.model

import kotlinx.serialization.Serializable
import mobi.waterdog.rest.template.validation.Validatable
import org.valiktor.Validator
import org.valiktor.functions.isBetween

@Serializable
data class Wheel(val diameter: Int, val width: Int) : Validatable<Wheel>() {

    override fun rules(validator: Validator<Wheel>) {
        validator.validate(Wheel::diameter)
            .isBetween(10, 20)
        validator.validate(Wheel::width)
            .isBetween(125, 255)
    }
}
