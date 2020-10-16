package test.ktortemplate.core.model


import org.valiktor.Constraint
import org.valiktor.ConstraintViolationException
import org.valiktor.Validator
import org.valiktor.constraints.*
import test.ktortemplate.core.exception.AppException
import test.ktortemplate.core.exception.ErrorCode
import test.ktortemplate.core.exception.ErrorDefinition

abstract class Validatable<T> {

    internal abstract fun validationSpec(obj: Validator<T>? = null)

    private fun minMaxMap(min: Any?, max: Any?) =
        mapOf("min" to min.toString(), "max" to max.toString())

    private fun valueMap(value: Any?) = mapOf("value" to value.toString())

    private fun violatedConstraint2map(valiktorConstraint: Constraint): Map<String, String> =
        when(valiktorConstraint) {
            is Between<*>           -> minMaxMap(valiktorConstraint.start, valiktorConstraint.end)
            is NotBetween<*>        -> minMaxMap(valiktorConstraint.start, valiktorConstraint.end)
            is GreaterOrEqual<*>    -> valueMap(valiktorConstraint.value)
            is Greater<*>           -> valueMap(valiktorConstraint.value)
            is LessOrEqual<*>       -> valueMap(valiktorConstraint.value)
            is Less<*>              -> valueMap(valiktorConstraint.value)
            else                    -> mapOf()
        }

    private fun valiktorException2AppException(valiktorEx: ConstraintViolationException): AppException {
        val validationException = AppException(
            code = ErrorCode.InvalidUserInput,
            title = "Invalid content for class ${this::class.qualifiedName}, check invalid fields in errors")

        validationException.errors.addAll(
            valiktorEx.constraintViolations
                .map {
                    ErrorDefinition(
                        "errors.validation.${it.property}.${it.constraint.name}",
                        it.property,
                        violatedConstraint2map(it.constraint)) }
                .toList())
        return validationException
    }

    fun validate() {
        try {
            validationSpec()
        } catch (ex: ConstraintViolationException) {
            throw valiktorException2AppException(ex)
        }
    }
}