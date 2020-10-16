package test.ktortemplate.core.model

import org.valiktor.Validator
import org.valiktor.functions.isBetween

data class Wheel(val diameter: Int, val width: Int): Validatable<Wheel>() {

    override fun validationSpec(obj: Validator<Wheel>?) {
        val target = obj ?: Validator(this)
        target.validate(Wheel::diameter)
            .isBetween(10, 20)
        target.validate(Wheel::width)
            .isBetween(125, 255)
    }
}