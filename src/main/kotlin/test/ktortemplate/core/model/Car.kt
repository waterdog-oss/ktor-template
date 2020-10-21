package test.ktortemplate.core.model

import org.valiktor.Validator
import org.valiktor.functions.hasSize
import org.valiktor.functions.isIn
import org.valiktor.functions.validateForEach

data class Car(val id: Long, val brand: String, val model: String, val wheels: List<Wheel>? = null) : Validatable<Car>() {

    override fun rules(validator: Validator<Car>) {
        validator
            .validate(Car::brand)
            .hasSize(3, 20)
            .isIn("porsche", "lamborghini", "koenigsegg")
        validator
            .validate(Car::wheels)
            .hasSize(3, 6)
            .validateForEach { it.applyRules(this) }
    }
}

data class CarSaveCommand(val brand: String, val model: String)

data class CarSaveCommandV1(val brand: String)
