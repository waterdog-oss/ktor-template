package test.ktortemplate.core.model

import com.capraro.kalidation.constraints.function.inValues
import com.capraro.kalidation.constraints.function.notBlank
import com.capraro.kalidation.dsl.constraints
import com.capraro.kalidation.dsl.property
import com.capraro.kalidation.dsl.validationSpec
import com.capraro.kalidation.spec.ValidationSpec

data class Car(val id: Long, val brand: String, val model: String) : Validatable() {
    override val spec: ValidationSpec
        get() = validationSpec {
            constraints<Car> {
                property(Car::brand) {
                    notBlank()
                    inValues("porsche", "lamborghini", "koenigsegg")
                    // TODO inValues should accept a list, issue reported at https://github.com/rcapraro/kalidation/issues/12
                    //  Another nice feature would be validate related properties (car models by brand for instance).
                    //  Already reported at https://github.com/rcapraro/kalidation/issues/9
                }
            }
        }
}

data class CarSaveCommand(val brand: String, val model: String)


