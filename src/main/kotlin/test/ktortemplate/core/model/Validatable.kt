package test.ktortemplate.core.model

import arrow.core.Invalid
import com.capraro.kalidation.spec.ValidationSpec
import com.fasterxml.jackson.annotation.JsonIgnore
import test.ktortemplate.core.exception.AppException
import test.ktortemplate.core.exception.ErrorCode

abstract class Validatable {
    @get:JsonIgnore
    abstract val spec: ValidationSpec
}

fun Validatable.validate() {
    val validationResult = spec.validate(this)
    if (validationResult is Invalid) {
        val validationException = AppException(
            code = ErrorCode.InvalidUserInput,
            title = "Invalid content for class ${this.javaClass.canonicalName}, check invalid fields in params"
        )
        validationResult.e.forEach {
            validationException.params[it.fieldName] =
                listOf(it.message, "Received values:", it.invalidValue.toString())
        }
        throw validationException
    }
}