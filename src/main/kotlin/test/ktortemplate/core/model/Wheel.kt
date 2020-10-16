package test.ktortemplate.core.model

import org.valiktor.Validator
import org.valiktor.functions.isBetween

data class Wheel(val diameter: Int, val width: Int): Validatable<Wheel>() {

    override fun rules(validator: Validator<Wheel>) {
        validator.validate(Wheel::diameter)
            .isBetween(10, 20)
        validator.validate(Wheel::width)
            .isBetween(125, 255)
    }
}