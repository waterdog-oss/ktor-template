package test.ktortemplate.core.model

import org.valiktor.Validator
import org.valiktor.functions.hasSize
import org.valiktor.functions.isIn
import org.valiktor.functions.validateForEach
import org.valiktor.validate

data class Car(val id: Long, val brand: String, val model: String, val wheels: List<Wheel> = listOf()) : Validatable<Car>() {

    override fun validationSpec(obj: Validator<Car>?) {

        val rules: (validator: Validator<Car>) -> Unit = { validator: Validator<Car> ->
            validator.validate(Car::brand)
                .hasSize(3, 20)
                .isIn("porsche", "lamborghini", "koenigsegg")
            validator.validate(Car::wheels)
                .hasSize(3, 6)
                .validateForEach { it.validationSpec(this)}

        }

        obj?.let{
            rules(it)
        } ?: validate(this) { rules(this) }
    }
}

data class CarSaveCommand(val brand: String, val model: String)


